var addButton = document.getElementById("add-button");
addButton.addEventListener("click", temp);
var sellUsdTransactions = [];
var buyUsdTransactions = [];
var selectt = document.getElementById("transaction-type");
var a = document.getElementById("buy-usd-rate");
var b = document.getElementById("sell-usd-rate");
var c = document.getElementById("lbp-amount");
var d = document.getElementById("usd-amount");
var SERVER_URL = "http://127.0.0.1:5000"


async function addItem() {
    var lbp = c.value;
    var usd = d.value;
    var op = true;
    if (selectt.value=="usd-to-lbp"){
        op=true;
    }
    else{
        op=false;
    }
    const data = {usd_amount : usd , lbp_amount : lbp , usd_to_lbp:op};
    
    const response = await  fetch(`${SERVER_URL}/transaction`, {
        method: 'POST', // *GET, POST, PUT, DELETE, etc.
        mode: 'cors', // no-cors, *cors, same-origin
        headers: {
          'Content-Type': 'application/json'
          // 'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: JSON.stringify(data)
        
      });
      fetchRates();
    return response.json();

    
}


function temp(){
    addItem()
  .then(data => {
    console.log(data); // JSON data parsed by `data.json()` call
  });
  fetchRates();
}
 

//





function fetchRates() {
    fetch(`${SERVER_URL}/exchangeRate`)
    .then(response => response.json())
    .then(data => { b.innerHTML = data.usd_to_lbp; a.innerHTML =data.lbp_to_usd ;
    });
   }
   fetchRates();

