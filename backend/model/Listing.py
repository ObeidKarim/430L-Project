from app import db,ma

class Listing(db.Model):
    listing_id = db.Column(db.Integer, primary_key = True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable = False)
    usd_amount = db.Column(db.Float,nullable = False)
    rate = db.Column(db.Float, nullable = False)
    usd_to_lbp = db.Column(db.Boolean, nullable = False)

class ListingSchema(ma.Schema):
    class Meta:
        fields = ("listing_id", "user_id", "usd_amount","rate","usd_to_lbp")
        model = Listing