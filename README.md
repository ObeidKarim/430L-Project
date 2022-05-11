# Backend

## Setup
Follow the steps below to setup your backend:
1. Clone the repo:
    1. Create a new folder anywhere in your PC.
    2. Open Command Prompt and change the path to the new folder created.
    3. Using the Command Prompt, write : git clone https://github.com/ObeidKarim/430L-Project
2. Make sure you have Flask installed. Check this link to know more : https://phoenixnap.com/kb/install-flask
3. By now, you'll find the folder cloned into your new folder. Using Command Prompt, write cd/exchange-rate/backend
4. To install the dependencies needed to run the application, run "pip install -r requirements.txt" using Command Prompt. Note that this step is done only once
5. To run the flask application, write:
    1. set FLASK_APP=server.py
    2. flask run
## Database Models
The database has 3 models:
* User
    * Attributes :
        * Id : Primary Key
        * user_name
        * password
* Transaction
    * Attributes :
        * Id : Primary Key
        * user_id : Foreign Key to User(id)
        * usd_amount
        * lbp_amount
        * usd_to_lbp
* UserTransaction
    * Attributes :
        * transaction_id : Primary Key and Foreign Key to Transaction(id)
        * user1_id : Foreign Key to User(id)
        * user2_id : Foreign Key to User(id)

# Mobile App
## Setup
To run the app, you need to:

1- Download the latest version of Android Studio. 2- Install the device emulator. Within Android Studio, go to ‘Tools’ -> ‘SDK Manager’ -> ‘SDK Tools’ and download ‘Android Emulator’.
3- Within ‘Tools’ -> ‘AVD Manager’, create a virtual device to be used for simulation. Select a physical hardware setup to emulate, and then in the next section download the newest software profile, API level 30.

## Features
### User authentication
Registration and login pages for registering and authentication users.
### Exchange Page
* Check current exchange rates (Buy and Sell Usd Rates)
* Calculator that calculates the buy and sell amount of a secif lbp or usd input
### Statistics Page
* Displays various statistics (number of transactions, volume, median...)
### Transactions Page
Listing Trades of other users, and can accept a trade with another user.
### History
Displays all transactions of the logged-in user
