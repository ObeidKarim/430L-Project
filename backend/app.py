import datetime

from flask import Flask
from  flask_sqlalchemy import SQLAlchemy
from flask import request
from flask import jsonify
from flask_cors import CORS
from flask_marshmallow import Marshmallow
from flask_bcrypt import Bcrypt
from flask import abort
import jwt
from . import db_config

app = Flask(__name__)
ma = Marshmallow(app)
app.config['SQLALCHEMY_DATABASE_URI'] = db_config.DB_CONFIG
CORS(app)
db = SQLAlchemy(app)
bcrypt = Bcrypt(app)

from .model.user import User, UserSchema
from .model.transaction import Transaction,TransactionSchema

transaction_schema = TransactionSchema()
transactions_schema = TransactionSchema(many=True)
user_schema = UserSchema()

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
 request_data = request.get_json()
 user_name = request_data['user_name']
 password = request_data['password']

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


#volume over 3 days
#highest transaction
#number of transactions


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

 buyUsdTransactions = Transaction.query.filter(Transaction.usd_to_lbp == False).all()
 listOfCoordinates = []
 for trans in buyUsdTransactions:
   listOfCoordinates.append(getCoordinatesFromTransaction(trans))

 return jsonify(coordinates_schema.dump(listOfCoordinates))



