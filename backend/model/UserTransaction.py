from app import db,ma,bcrypt

class UserTransaction(db.Model):
    transaction_id = db.Column(db.Integer, db.ForeignKey('transaction.id'),primary_key = True)
    user1_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    user2_id = db.Column(db.Integer, db.ForeignKey('user.id'))
