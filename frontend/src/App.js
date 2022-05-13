import { useState , useCallback } from "react";
import { useEffect } from "react";
import AppBar from '@material-ui/core/AppBar'
import Typography from '@material-ui/core/Typography'
import Toolbar from '@material-ui/core/Toolbar'
import Button from '@material-ui/core/Button'
import UserCredentialsDialog from './UserCredentialsDialog/UserCredentialsDialog.js'
import { getUserToken,saveUserToken, clearUserToken } from "./localStorage";
import { DataGrid ,GridColDef, GridValueGetterParams } from '@mui/x-data-grid';

import React, { PureComponent } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';



import Snackbar from "@material-ui/core/Snackbar";

import ReactHighcharts from 'react-highcharts/ReactHighstock.src'
import moment from 'moment'

import { AgGridReact } from 'ag-grid-react';
import 'ag-grid-community/dist/styles/ag-grid.css';
import 'ag-grid-community/dist/styles/ag-theme-alpine.css';

import './App.css';


var SERVER_URL = "http://127.0.0.1:5000"


function App()  {
//states for the fetchRates function
let [buyUsdRate, setBuyUsdRate] = useState(null);
let [sellUsdRate, setSellUsdRate] = useState(null);

//States for adding new transaction
let [lbpInput, setLbpInput] = useState("");
let [usdInput, setUsdInput] = useState("");
let [transactionType, setTransactionType] = useState("usd-to-lbp");

//States for adding new listing
let [rateListingInput, setRateListingInput] = useState("");
let [usdListingInput, setUsdListingInput] = useState("");
let [transactionListingType, setTransactionListingType] = useState("usd-to-lbp");

//States for the calculator
let [resultOutput , setResultOutput] = useState(null);
let [amountInput , setamountInput] = useState("");

//State for user token
let [userToken, setUserToken] = useState(getUserToken());

//State for getting the user transactions
let [userTransactions, setUserTransactions] = useState([]);

//state for getting all transactions
let [Transactions, setTransactions] = useState([]);

//State for getting all the Listings
let [Listings, setListings] = useState([]);

//state for the row data
let [aggridRowData, setAggridRowData] = useState([]);

//state for getting the statistics
let [rateStatistics , SetRateStatistics]=useState([]);



const States = {
  PENDING: "PENDING",
  USER_CREATION: "USER_CREATION",
  USER_LOG_IN: "USER_LOG_IN",
  USER_AUTHENTICATED: "USER_AUTHENTICATED",
 };
let [authState, setAuthState] = useState(States.PENDING);

//fetch rates for the user
function fetchRates() {
  fetch(`${SERVER_URL}/exchangeRate`)
  .then(response => response.json())
  .then(data => { setSellUsdRate( data.usd_to_lbp); setBuyUsdRate(data.lbp_to_usd);});
 }
 useEffect(fetchRates, []);

 //get all the Listings made by users
 const fetchListings = useCallback(() => {
  fetch(`${SERVER_URL}/listings`, {
    headers: {
    Authorization: `bearer ${userToken}`,
    }
    })
  .then((response) => response.json())
  .then((data) => {
    setListings(data);
    
  });
  
  } , [userToken]);
  useEffect(fetchListings,[]);


  //get the statistics of the rates
function fetchStatistics() {
  fetch(`${SERVER_URL}/statistics`)
  .then(response => response.json())
  .then(data => { SetRateStatistics(data);});
 }
 useEffect(fetchStatistics, []);


 //get transactions made by current user only
  const fetchUserTransactions = useCallback(() => {
  fetch(`${SERVER_URL}/transaction`, {
  headers: {
  Authorization: `bearer ${userToken}`,
  }
  })
  .then((response) => response.json())
  .then((data) => {setUserTransactions(data)
  
  });
  }, [userToken]);
  useEffect(() => {
  if (userToken) {
  fetchUserTransactions();
  }
  }, [fetchUserTransactions, userToken]);

  //get all transactions made by all users for the highchart
  const fetchTransactions = useCallback(() => {
    fetch(`${SERVER_URL}/transactionall`)
    .then((response) => response.json())
    .then((data) => {setTransactions(data)});
    });
    useEffect(fetchTransactions , []);

  //function for the calculator button: the user enters the amount of LBP he has and the calculator gives him the amount of usd he would get
 function testOutput(){
  var amt = amountInput;
  var op = false;
  if (transactionType=="usd-to-lbp"){
       op=true;
   }
  else{
       op=false;
   }
   if (op){
     var temp = amt * sellUsdRate;
     setResultOutput(temp+ " L.B.P");
   }
   else{
     var temp = amt / buyUsdRate;
     setResultOutput(temp + " $");
   }
}

//function to add transaction for 1 user
async function addItem() {
 var lbp = lbpInput;
 var usd = usdInput;
 var op = false;
 if (transactionType=="usd-to-lbp"){
     op=true;
 }
 else{
     op=false;
 }
 var bear=""
 if (userToken){
   bear="Bearer " + userToken;
 }
 const data = { 'usd_amount' :  usd , 'lbp_amount' : lbp , 'usd_to_lbp':op};
 
 const response = await  fetch(`${SERVER_URL}/transaction`, {
     method: 'POST', // *GET, POST, PUT, DELETE, etc.
     mode: 'cors', // no-cors, *cors, same-origin
     headers: {
       'Content-Type': 'application/json',
       'Authorization': bear
       
       // 'Content-Type': 'application/x-www-form-urlencoded',
     },
     body: JSON.stringify(data)
     
   });
   fetchRates();

 return response.json();
}

//function to accept listing
async function acceptListing  (params){
  var listingUserId = params.data.user;
  if(currentUserId == listingUserId){
    return;
  }
 var listingid = params.data.listing_id;
 var bear=""
 if (userToken){
   bear="Bearer " + userToken;
 }
 const data = { 'listing_id' : listingid};
 
 const response = await fetch(`${SERVER_URL}/acceptListing`, {
     method: 'POST', // *GET, POST, PUT, DELETE, etc.
     mode: 'cors', // no-cors, *cors, same-origin
     headers: {
       'Content-Type': 'application/json',
       'Authorization': bear
       
       // 'Content-Type': 'application/x-www-form-urlencoded',
     },
     body: JSON.stringify(data)
     
   });
   fetchListings();
   
 return ; 
}

//function to add listing for 1 user
async function addListingItem() {
 var rate = rateListingInput;
 var usd = usdListingInput;
 var op = false; 
 if (transactionListingType=="usd-to-lbp"){
     op=true;
 }
 else{
     op=false;
 }
 
 var bear=""
 if (userToken){
   bear="Bearer " + userToken;
 }
 const data = { 'usd_amount' :  usd , 'rate' : rate , 'usd_to_lbp':op};
 
 const response = await  fetch(`${SERVER_URL}/listing`, {
     method: 'POST', // *GET, POST, PUT, DELETE, etc.
     mode: 'cors', // no-cors, *cors, same-origin
     headers: {
       'Content-Type': 'application/json',
       'Authorization': bear
       
       // 'Content-Type': 'application/x-www-form-urlencoded',
     },
     body: JSON.stringify(data)
     
   });
   
   
   fetchListings();
 return response.json();

}






//function to login
function login(username, password) {
 return fetch(`${SERVER_URL}/authentication`, {
 method: "POST",
 headers: {
 "Content-Type": "application/json",
 
 },
 body: JSON.stringify({
 user_name: username,
 password: password,
 }),
 })
 .then((response) => response.json())
 .then((body) => {
 setAuthState(States.USER_AUTHENTICATED);
 setUserToken(body.token);
 saveUserToken(body.token);
 //fetchRates();
 });
 
 }

 //function to create user
 function createUser(username, password) {
   return fetch(`${SERVER_URL}/user`, {
   method: "POST",
   headers: {
   "Content-Type": "application/json",
   
   },
   body: JSON.stringify({
   user_name: username,
   password: password,
   }),
   }).then((response) => login(username, password));
   }

   //function to logout
   function logout() {
     setUserToken(null);
     clearUserToken();
    }


//DataGrid Table of all the transactions made by the user: has 2 main components: column and row
  //column for the user transactions table
  const column= [
    {
      field: 'usd_amount',
      headerName: 'usd-amount',
      width: 150,
      
    },
    {
      field: 'lbp_amount',
      headerName: 'Lbp-amount',
      width: 150,
    },
    {
      field: 'usd_to_lbp',
      headerName: 'usd-to-lbp',
      width: 110,
      
    },
    {
      field: 'user_id',
      headerName: 'userID',
      width: 160,
    },
    {
      field: 'added_date',
      headerName: 'date',
      width: 160,
    } 
  ];
  //rows of the user transactions table
  //s is the variable containig all the transactions done by the user: Array of transactions: Each transaction contains usd_amount, lbp_amount, added_date, usd_to_lbp, user_id
  var s =(userTransactions);
  const  row = [];
  var ii = 1;
  var currentUserId = 0;
  //adding the user transactions to the array called row which wil be the rows of the table corresponding for the user transactions
  for(var index=0 ; index < s.length ; index++){
    row.push({usd_amount:s[index].usd_amount, lbp_amount:(s)[index].lbp_amount, added_date:s[index].added_date ,  usd_to_lbp:s[index].usd_to_lbp , user_id:s[index].user_id ,id:ii});
    ii+=1;
    currentUserId = s[index].user_id;  
  }


//highcharts Line Graph to show the rate fluctuation over time 

//variable alltransactions is an array that contains all the transactions done by all users
//we will use this to calculate all the rates and get the date of each rate, this way we will have the data we need to show the 
//fluctuation of rates over time
var alltransactions = Transactions ;
const priceData = [];
//adding to priceData Array the rate and the date
//we add the date as an integer called timestamp which calculates the date according to year month day hour second
for(var index=0 ; index <alltransactions.length ; index++ ){
  var rateTemp = alltransactions[index].lbp_amount / alltransactions[index].usd_amount;
  var dateTemp = new Date(alltransactions[index].added_date);
  var timeStampTemp = dateTemp.getTime();
  priceData.push([timeStampTemp , rateTemp]);
}
//we sort the priceData array in increasing order of Date
priceData.sort(function(a, b) {
  return a[0] - b[0];
})

//documentation of highcharts
const options = {style: 'currency', currency: 'LBP'};
const numberFormat = new Intl.NumberFormat('en-US', options);
const configPrice = {
  
  yAxis: [{
    offset: 20,

    labels: {
      formatter: function () {
        return numberFormat.format(this.value) 
      }
      ,
      x: -15,
      style: {
        "color": "#000", "position": "absolute"

      },
      align: 'left'
    },
  },
    
  ],
  tooltip: {
    shared: true,
    formatter: function () {
      return numberFormat.format(this.y, 0) +  '</b><br/>' + moment(this.x).format('MMMM Do YYYY, h:mm')
    }
  },
  plotOptions: {
    series: {
      showInNavigator: true,
      gapSize: 6,

    }
  },
  rangeSelector: {
    selected: 1
  },
  title: {
    text: `USD/LBP exchange rate`
  },
  chart: {
    height: 600,
  },

  credits: {
    enabled: false
  },

  legend: {
    enabled: true
  },
  xAxis: {
    type: 'date',
  },
  rangeSelector: {
    buttons: [{
      type: 'day',
      count: 1,
      text: '1d',
    }, {
      type: 'day',
      count: 7,
      text: '7d'
    }, {
      type: 'month',
      count: 1,
      text: '1m'
    }, {
      type: 'month',
      count: 3,
      text: '3m'
    },
      {
      type: 'all',
      text: 'All'
    }],
    selected: 4
  },
  series: [{
    name: 'Rate',
    type: 'spline',

    data: priceData,
    tooltip: {
      valueDecimals: 2
    },

  }
  ]
};






     //ag grid transactions between users

    
     //ag grid columns
     const columnDefs= [
      { headerName: "Listing ID", field: "listing_id" },
      { headerName: "User", field: "user" },
      { headerName: "USD amount", field: "usdamount" },
      { headerName: "Rate", field: "rate" },
      {headerName: "Wants to Sell or Buy",field:"sellorbuy"},
      {headerName: "Action",
      //to add accept listing button
      cellRendererFramework:(params)=><div>
        <button className="button" type="button" onClick={()=>acceptListing(params)}>Accept Listing</button>
      </div>}
      ]

      // ag grid row data taken from the Listings
      //update the grid after adding or accepting a listing
      //

      function updateGrid(){
        const rowData= [];
        for(var index = 0 ; index < Listings.length ; index++){
          if(Listings[index].usd_to_lbp==true){
            rowData.push({listing_id: Listings[index].listing_id,user: Listings[index].user_id , usdamount: Listings[index].usd_amount, rate: Listings[index].rate , sellorbuy: "sell" })
          }
          else{
          rowData.push({listing_id: Listings[index].listing_id,user: Listings[index].user_id , usdamount: Listings[index].usd_amount, rate: Listings[index].rate , sellorbuy: "buy" })
          }
        }
        setAggridRowData(rowData);
      }
      //call updateGrid whenever the listings array change
      useEffect(()=>{updateGrid();} , [Listings]);
    

    
     
   

  
  return (
    <div>
     
      <UserCredentialsDialog open={authState==States.USER_CREATION} onSubmit={createUser} onClose={() =>setAuthState(States.PENDING)} title={'REGISTER'} submitText={'submit'}/>
      <UserCredentialsDialog open={authState==States.USER_LOG_IN} onSubmit={login} onClose={() =>setAuthState(States.PENDING)} title={'LOGIN'} submitText={'submit'}/>

<html>
<head>

</head>
<body>
    <div className="header">
    <AppBar position="static">
        <Toolbar classes={{ root: "nav" }}>
        <Typography variant="h5">LBP Exchange Tracker</Typography>
        <div>
        {userToken !== null ? (
 <Button color="inherit" onClick={logout}>
 Logout
 </Button>
 ) : (
 <div>
 <Button
 color="inherit"
 onClick={() => setAuthState(States.USER_CREATION)}
 >
 Register
 </Button>
 <Button
 color="inherit"
 onClick={() => setAuthState(States.USER_LOG_IN)}
 >
 Login
 </Button>
 </div>
 )}
        </div>

        </Toolbar>
      </AppBar>
    
    </div>
    <div className="wrapper">
        <h2>Today's Exchange Rate</h2>
        <h3>Buy USD: <span id="buy-usd-rate" value={buyUsdRate} onChange={e => setBuyUsdRate(e.target.value) }>
          {buyUsdRate}
        </span>
        </h3>
        <h3>Buy LBP: <span id="sell-usd-rate" value={sellUsdRate} onChange={e => setSellUsdRate(e.target.value)} >
          {sellUsdRate}
        </span>
        </h3>
        
        <hr />
        <h2>Check result of your transaction</h2>
        <h3>Result: <span id="result-amnt" value={resultOutput} onChange={e => setResultOutput(e.target.value) }>
          {resultOutput}
        </span></h3>

        

        <form name="transaction-entry">
            <div className="amount-input">
                <label htmlFor="amount-input">Amount</label>
                <input id="amount-input" type="number" value={amountInput} onChange={e =>setamountInput(e.target.value)}/>
            </div>
            <select id="transaction-type" value={transactionType} onChange={e =>setTransactionType(e.target.value)} >
                <option value="usd-to-lbp">I have USD</option>
                <option value="lbp-to-usd">I have LBP</option>
            </select>
               <button id="test-button" className="button" type="button" onClick={testOutput}>Test</button>
               
        </form>
        
        
        
    </div>
    <div className="wrapper">
    <p>LBP to USD Exchange Rate</p>
        
        <h2>Record a recent transaction</h2>
        <form name="transaction-entry">
            <div className="amount-input">
                <label htmlFor="lbp-amount">LBP Amount</label>
                <input id="lbp-amount" type="number" value={lbpInput} onChange={e =>setLbpInput(e.target.value)}/>
                <label htmlFor="usd-amount">USD Amount</label>
                <input id="usd-amount" type="number" value={usdInput} onChange={e =>setUsdInput(e.target.value)}/>

               </div>
               <select id="transaction-type" value={transactionType} onChange={e =>setTransactionType(e.target.value)} >
                <option value="usd-to-lbp">USD to LBP</option>
                <option value="lbp-to-usd">LBP to USD</option>
               </select>
               <button id="add-button" className="button" type="button" onClick={addItem}>Add</button>
               
        </form>
    </div>

    
    <div className="wrapper">
    <p>Exchange Rate Statistics</p>
    <h2>Statistics and Insights:</h2>
    <p><b>Total number of transactions:</b> {rateStatistics.numberOfTransactions}</p>
    <p><b>Total volume:</b> {rateStatistics.volume}</p>
    <br></br>
    <p><b>Highest LBP to USD rate reach:</b> {rateStatistics.max_lbp_to_usd}</p>
    <p><b>Highest USD to LBP rate reach:</b> {rateStatistics.max_usd_to_lbp}</p>
    <br></br>
    <p><b>Median LBP to USD rate:</b> {rateStatistics.median_lbp_to_usd}</p>
    <p><b>Median USD to LBP rate:</b> {rateStatistics.median_usd_to_lbp}</p>
    <br></br>
    <p><b>Mode LBP to USD rate:</b> {rateStatistics.mode_lbp_to_usd}</p>
    <p><b>Mode USD to LBP rate:</b> {rateStatistics.mode_usd_to_lbp}</p>
    <br></br>
    <p><b>Standard Deviation of the LBP to USD rate:</b> {rateStatistics.stdev_lbp_to_usd}</p>
    <p><b>Standard Deviation of the USD to LBP rate:</b> {rateStatistics.stdev_usd_to_lbp}</p>
    <br></br>
    <p><b>Variance of the LBP to USD rate:</b> {rateStatistics.variance_lbp_to_usd}</p>
    <p><b>Variance of the USD to LBP rate:</b> {rateStatistics.variance_usd_to_lbp}</p>



    <Snackbar
        elevation={6}
        variant="filled"
        open={authState === States.USER_AUTHENTICATED}
        autoHideDuration={2000}
        onClose={() => setAuthState(States.PENDING)}
        message='Success'
      >
    
      </Snackbar> 
    
    </div> 


    

    <script src="script.js"></script>
    
</body>
</html>

{userToken && (
 <div className="wrapper">
   
 <Typography variant="h5">Your Transactions</Typography>
 <p>{}</p>
 <DataGrid
 name = 'dgrid'
 rows={row}
 columns = {column}
 autoHeight
 />
 </div>
 )}



<div className = "wrapper">
         <ReactHighcharts config = {configPrice}></ReactHighcharts>
</div>

{userToken && (
<div className="wrapper">

<h2>Add a new Listing</h2>
<h3>You can add a new listing as User: {currentUserId}</h3>
        <form name="listing-entry">
            <div className="amount-input">
                <label htmlFor="rate-listing">Rate</label>
                <input id="rate-listing" type="number" value={rateListingInput} onChange={e =>setRateListingInput(e.target.value)}/>
                <label htmlFor="usd-amount-listing">USD Amount</label>
                <input id="usd-amount-listing" type="number" value={usdListingInput} onChange={e =>setUsdListingInput(e.target.value)}/>

               </div>
               <select id="transaction-listing-type" value={transactionListingType} onChange={e =>setTransactionListingType(e.target.value)} >
                <option value="usd-to-lbp">Sell USD</option>
                <option value="lbp-to-usd">Buy USD</option>
               </select>
               <button id="add-listing-button" className="button" type="button" onClick={addListingItem}>Add Listing</button>
               
        </form>

  <h2>USD/LBP listings</h2> 
<div className="ag-theme-alpine" style={ {height: '200px'} }>
        <AgGridReact
            columnDefs={columnDefs}
            rowData={aggridRowData}
            defaultColDef={{flex:1}}>
        </AgGridReact>
      </div>
</div>

)}



    </div>
  );
}







export default App;
