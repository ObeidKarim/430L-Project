import datetime

from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask import request, Blueprint
from flask import jsonify
from flask_cors import CORS
from itsdangerous import json
from flask_marshmallow import Marshmallow
from flask_bcrypt import Bcrypt
from flask import abort
import jwt
import db_config
import statistics
from flasgger import Swagger


app = Flask(__name__)
ma = Marshmallow(app)
app.config['SQLALCHEMY_DATABASE_URI'] = db_config.DB_CONFIG
CORS(app)
db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
swagger = Swagger(app)
app_blue = Blueprint('app', __name__, url_prefix='/app')


app.register_blueprint(app_blue)

from model.user import User, UserSchema
from model.transaction import Transaction,TransactionSchema
from model.UserTransaction import UserTransaction
from model.Listing import Listing,ListingSchema

transaction_schema = TransactionSchema()
transactions_schema = TransactionSchema(many=True)
user_schema = UserSchema()

listing_schema = ListingSchema()
listings_schema = ListingSchema(many= True)

def extract_auth_token(authenticated_request):
 auth_header = authenticated_request.headers.get('Authorization')
 if auth_header:
  return auth_header.split(" ")[1]
 else:
  return None


def decode_token(token):
 payload = jwt.decode(token, SECRET_KEY, 'HS256')
 return payload['sub']


@app.route('/transaction',methods=['POST'])
def transact():
 """ Registers a new transaction (user specific transaction if user is logged in, else registers transaction anonymously).
    ---
     parameters:
      - name: token
        in: header
        type : string
        required: true
        description : The token returned by the backend whenever a certain user signs in.
      - name: usd_amount
        in: body
        type : number
        example: 8
        required: true
      - name: lbp_amount
        in: body
        type : number
        example: 100000
        required: true
      - name : usd_to_lbp
        in : body
        type : boolean
        example : 1
        required : true
        description : True if the transaction is USD to LBP. False otherwise.   
     responses:
       200:
         description: The transaction added as a json.
       400:
         description : The input is invalid.
    """ 

 try:
  request_data = request.get_json()
  usd_amount = request_data['usd_amount']
  lbp_amount = request_data['lbp_amount']
  usd_to_lbp = request_data['usd_to_lbp']
 except (TypeError,KeyError):
  abort(400)

 token = extract_auth_token(request)
 user_id = None
 if token is not None:
  try:
   user_id = decode_token(token)
  except (jwt.ExpiredSignatureError,jwt.InvalidTokenError):
   abort(403, 'Invalid Token')

 new_transaction = Transaction(usd_amount = usd_amount, lbp_amount = lbp_amount,usd_to_lbp= usd_to_lbp,user_id=user_id)
 db.session.add(new_transaction)
 db.session.commit()
 return jsonify(transaction_schema.dump(new_transaction))


@app.route('/transaction',methods=['GET'])
def get_transations():
 """ Returns all transactions of logged in user.
    ---
     parameters:
      - name: token
        in: header
        type : string
        required: true
        description : The token returned by the backend whenever a certain user signs in.
     responses:
       200:
         description: returns list of transactions of user
       403:
         description : Invalid Token
    """ 
 token = extract_auth_token(request)
 if token is None:
  abort(403,'Must include authorization token')

 try:
  user_id = decode_token(token)
 except (jwt.ExpiredSignatureError, jwt.InvalidTokenError):
  abort(403, 'Invalid Token')

 list_of_transactions = Transaction.query.filter_by(user_id = user_id).all()

 return jsonify(transactions_schema.dump(list_of_transactions))


#takes a list of Transaction object, returns a list of rates
def pushRates(TransactionsList):
 rate = []
 for x in TransactionsList:
  rate.append(x.lbp_amount/x.usd_amount)
 return rate

@app.route('/exchangeRate',methods = ['GET'])
def getExchangeRate():
 """ Returns the exchange rates during a last 3 days. 
    ---
    responses:
      200:
        description: The exchange rate during the last 3 days. Returns both usd_to_lbp and lbp_to_usd rates.
    """ 
 END_DATE = datetime.datetime.now()
 START_DATE = END_DATE - datetime.timedelta(days = 3)

 buyUsdTransactions = Transaction.query.filter(Transaction.added_date.between(START_DATE, END_DATE),
                                                Transaction.usd_to_lbp == False
                                                ).all()


 sellUsdTransactions = Transaction.query.filter(Transaction.added_date.between(START_DATE, END_DATE),
                                                Transaction.usd_to_lbp == True
                                                ).all()

 buyUsdRates = pushRates(buyUsdTransactions)
 sellUsdRates = pushRates(sellUsdTransactions)

 if len(buyUsdRates) != 0:
  buyRate = round(sum(buyUsdRates)/len(buyUsdRates),2)
 else:
  buyRate = "Not available yet"

 if len(sellUsdRates) != 0:
  sellRate = round(sum(sellUsdRates)/len(sellUsdRates),2)
 else:
  sellRate = "Not available yet"

 jsonResponse = jsonify(usd_to_lbp = sellRate, lbp_to_usd = buyRate)

 return jsonResponse

@app.route('/user',methods = ['POST'])
def addUser():
  """ Creates and Signs up a new User.
    ---
    parameters:
      - name: user_name
        in: body
        type : string
        example: KarimBH
        required: true
      - name: password
        in: body
        type : string
        example: mepass
        required: true
    responses:
      200:
        description: The user_name and the user's id as a json
      400:
        description : The input is invalid. Make sure you have passed user_name and password.
    """ 

  try:
    request_data = request.get_json()
    user_name = request_data['user_name']
    password = request_data['password']
  
  except (KeyError,TypeError):
    abort(400)

  new_user = User(user_name, password)
  db.session.add(new_user)
  db.session.commit()

  return jsonify(user_schema.dump(new_user))


SECRET_KEY = "b'|\xe7\xbfU3`\xc4\xec\xa7\xa9zf:}\xb5\xc7\xb9\x139^3@Dv'"

def create_token(user_id):
 payload = {
  'exp': datetime.datetime.utcnow() + datetime.timedelta(days=4),
  'iat': datetime.datetime.utcnow(),
  'sub': user_id
 }
 return jwt.encode(
   payload,
   SECRET_KEY,
   algorithm='HS256')

@app.route('/authentication',methods = ['POST'])
def authenticate():
 """ Authenticates user's credentials. Used when a user wants to log in.
    ---
    parameters:
      - name: user_name
        in: body
        type : string
        example: KarimBH
        required: true
      - name: password
        in: body
        type : string
        example: mepass
        required: true
    responses:
      200:
        description: A token, to be used later by the user.
      400:
        description : The input is invalid. Make sure you have passed the correct user_name and password.
      403:
        description : Wrong credentials.
    """ 
 try:
     request_data = request.get_json()
     user_name = request_data['user_name']
     password = request_data['password']
 except (TypeError,KeyError):
    abort(400,"Body should contain user_name and password")


 if (user_name is None) or (password is None):
   abort(400,"Username or password cannot be null")

 user_data = User.query.filter_by(user_name=user_name).first()

 if  user_data is None:
    abort(403,'Username not found')

 if not bcrypt.check_password_hash(user_data.hashed_password, password):
   abort(403,'Password Incorrect')

 token = create_token(user_data.id)

 return jsonify(token=token)



class CoordinateSchema(ma.Schema):
 class Meta:
  fields = ("x","y")

coordinates_schema = CoordinateSchema(many= True)

def getCoordinatesFromTransaction(trans):
 END_DATE = trans.added_date
 START_DATE = END_DATE - datetime.timedelta(days = 3)
 buyTransactions = Transaction.query.filter(Transaction.added_date.between(START_DATE, END_DATE),
                          Transaction.usd_to_lbp == False ).all()
 buyRates = pushRates(buyTransactions)

 rate = sum(buyRates)/len(buyRates)

 return {"x":START_DATE, "y":rate}


@app.route('/graph', methods = ['GET'])
def getCoordinates():
 """Returns a of coordinates of the points to graph.
 ---
      responses:
        200:
          description: return list of coordinates.
 """ 

 buyUsdTransactions = Transaction.query.filter(Transaction.usd_to_lbp == False).all()
 listOfCoordinates = []
 for trans in buyUsdTransactions:
   listOfCoordinates.append(getCoordinatesFromTransaction(trans))

 return jsonify(coordinates_schema.dump(listOfCoordinates))


@app.route('/userTransaction/<username>',methods = ['POST'])
def add_user_transaction(username):
  """ Registers a new transaction of the logged in user and associates it with another user.
    ---
     parameters:
      - name: token
        in: header
        type : string
        required: true
        description : The token returned by the backend whenever a certain user signs in.
      - name: usd_amount
        in: body
        type : number
        example: 8
        required: true
      - name: lbp_amount
        in: body
        type : number
        example: 100000
        required: true
      - name : usd_to_lbp
        in : body
        type : boolean
        example : 1
        required : true
        description : True if the transaction is USD to LBP. False otherwise.
      - name : user2_id
        in : body
        type : string
        example : KarimObeid
        required : true
        description : The associated user in the transaction     
     responses:
       200:
         description: The transaction added as a json.
       400:
         description : The input is invalid.
    """
  token = extract_auth_token(request)
  if token is None:
    abort(403, 'Please Sign In')

  try:
    request_data = request.get_json()
    usd_amount = request_data['usd_amount']
    lbp_amount = request_data['lbp_amount']
    usd_to_lbp = request_data['usd_to_lbp']

    user1_id = decode_token(token)
    user2_id = User.query.filter_by(user_name = username).first().id

    if usd_amount and lbp_amount and usd_to_lbp is not None:
      new_transaction = Transaction(usd_amount = usd_amount, lbp_amount = lbp_amount,usd_to_lbp= usd_to_lbp,user_id=user1_id)
      db.session.add(new_transaction)
      db.session.commit()

      new_user_transaction = UserTransaction(user1_id = user1_id, user2_id=user2_id,transaction_id = new_transaction.id)

      db.session.add(new_user_transaction)
      db.session.commit()

    return jsonify(transaction_schema.dump(new_transaction))


  except (TypeError,KeyError):
    abort(400)

  except (jwt.ExpiredSignatureError,jwt.InvalidTokenError):
    abort(403, "Invalid Token")


@app.route('/listings',methods= ['GET'])
def get_listings():
  """Returns a list of all listings available.
    ---
        responses:
          200:
            description: return a list of listings.
  """
  list_of_listings = Listing.query.filter().all()
  return jsonify(listings_schema.dump(list_of_listings))

  
@app.route('/listing',methods = ['POST'])
def add_listing():
  """Adds a listing to the list of listings 
    ---
    parameters:
      - name: token
        in: header
        type : string
        required: true
        description : The token returned by the backend whenever a certain user signs in.
      - name: usd_amount
        in: body
        type : number
        example: 8
        required: true
      - name : usd_to_lbp
        in : body
        type : boolean
        example : 1
        required : true
        description : True if the transaction is USD to LBP. False otherwise.
      - name : rate
        in : body
        type : number
        example : 4000
        required : true
        description : rate of usd to lbp wanted by user.
      - name : user_id
        in : body
        type : string
        example : KarimObeid
        required : true
        description : The logged in user creating the transaction listing
    responses:
      200:
        description: Listing added
      400:
        description: Invalid input
      403:
        description: Invalid token
  
  """
  token = extract_auth_token(request)
  if token is None:
    abort(403)

  try:
    request_data = request.get_json()
    usd_amount = request_data['usd_amount']
    rate = request_data['rate']
    usd_to_lbp = request_data['usd_to_lbp']

    user_id = decode_token(token)

    if usd_amount and rate and usd_to_lbp is not None:
      new_listing = Listing(user_id = user_id,
                            usd_amount = usd_amount,
                            usd_to_lbp = usd_to_lbp,
                            rate = rate)
      db.session.add(new_listing)
      db.session.commit()
    
    return jsonify(listing_schema.dump(new_listing))

  except (TypeError,KeyError):
    abort(400)

  except (jwt.ExpiredSignatureError,jwt.InvalidTokenError):
    abort(403, "Invalid Token")


@app.route('/acceptListing', methods = ['POST'])
def acceptListing():
  """Accepts a listing from the list of listings, and creates a new user transaction.
    ---
        responses:
          200:
            description: registers the new transaction accepted.


  """
  token = extract_auth_token(request)
  if token is None:
    abort(403)

  try:
    request_data = request.get_json()
    listing_id = request_data['listing_id']

    user1_id = decode_token(token) 
    listing = Listing.query.filter_by(listing_id = listing_id ).first()

    usd_amount = listing.usd_amount
    rate = listing.rate
    usd_to_lbp = listing.usd_to_lbp
    user2_id = listing.user_id
    
    new_transaction = Transaction(usd_amount = usd_amount, lbp_amount = usd_amount*rate, usd_to_lbp= usd_to_lbp,user_id=user1_id)
    db.session.add(new_transaction)
    db.session.commit()
   

    new_user_transaction = UserTransaction(user1_id = user1_id, user2_id=user2_id,transaction_id = new_transaction.id)

    db.session.add(new_user_transaction)
  
    db.session.commit()

    Listing.query.filter_by(listing_id = listing_id).delete()
    db.session.commit()

   

    return jsonify(transaction_schema.dump(new_transaction))

  except (TypeError,KeyError):
    abort(400)

  except (jwt.ExpiredSignatureError,jwt.InvalidTokenError):
    abort(403, "Invalid Token")



@app.route('/users', methods = ['GET'])
def get_users():
  """ Returns a list of all registered users.
    ---
    parameters:
      - name: token
        in: header
        type : string
        required: true
        description : The token returned by the backend whenever a certain user signs in.
    responses:
      200:
        description: A json of all users registered
      403:
        description: Invalid Token
    """ 
  token = extract_auth_token(request)
  if token is None:
    abort(403)

  try:
    user_id = decode_token(token)
    users = User.query.filter(User.id != user_id).all()
    usernames = []
    for user in users:
      usernames.append(user.user_name)

    print(users)
    return jsonify(usernames)


  except:
    abort(403)


@app.route('/statistics', methods = ['GET'])
def get_stats():
  """Returns the volume, number (count), max, median, stdev, mode and variance of transactions.
    ---
      responses:
        200:
          description: A json of all stats
  
  
  """
  END_DATE = datetime.datetime.now()
  START_DATE = END_DATE - datetime.timedelta(days = 3)
  buyUsdTransactions = Transaction.query.filter(Transaction.added_date.between(START_DATE, END_DATE),
                                                Transaction.usd_to_lbp == False
                                                ).all()


  sellUsdTransactions = Transaction.query.filter(Transaction.added_date.between(START_DATE, END_DATE),
                                                Transaction.usd_to_lbp == True
                                                ).all()

  buy_rates = pushRates(buyUsdTransactions)
  sell_rates = pushRates(sellUsdTransactions)

  volume = 0
  numberOfTransactions = len(buyUsdTransactions) + len(sellUsdTransactions)
  for transaction in buyUsdTransactions:
    volume += transaction.usd_amount
  for transaction in sellUsdTransactions:
    volume += transaction.usd_amount

  stats = {}
  stats["volume"] = volume
  stats["numberOfTransactions"] = numberOfTransactions

  if (len(buy_rates) > 0):
    stats["max_usd_to_lbp"] = max(buy_rates)
    stats["median_usd_to_lbp"] = statistics.median(buy_rates)
    stats["stdev_usd_to_lbp"] = statistics.stdev(buy_rates)
    stats["mode_usd_to_lbp"] = statistics.mode(buy_rates)
    stats["variance_usd_to_lbp"] = statistics.variance(buy_rates)

  else:
    stats["max_usd_to_lbp"] = -1
    stats["median_usd_to_lbp"] = -1
    stats["stdev_usd_to_lbp"] = -1
    stats["mode_usd_to_lbp"] = -1
    stats["variance_usd_to_lbp"] = -1

  if (len(sell_rates) > 0):
    stats["max_lbp_to_usd"] = max(sell_rates)
    stats["median_lbp_to_usd"] = statistics.median(sell_rates)
    stats["stdev_lbp_to_usd"] = statistics.stdev(sell_rates)
    stats["mode_lbp_to_usd"] = statistics.mode(sell_rates)
    stats["variance_lbp_to_usd"] = statistics.variance(sell_rates)

  else:
    stats["max_lbp_to_usd"] = -1
    stats["median_lbp_to_usd"] = -1
    stats["stdev_lbp_to_usd"] = -1
    stats["mode_lbp_to_usd"] = -1
    stats["variance_lbp_to_usd"] = -1

  return jsonify(stats)










                                   

