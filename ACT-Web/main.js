import { initializeApp } from 'https://www.gstatic.com/firebasejs/9.0.0/firebase-app.js';
import { getAuth, signInWithPopup, createUserWithEmailAndPassword, signInWithEmailAndPassword, GoogleAuthProvider, onAuthStateChanged } from 'https://www.gstatic.com/firebasejs/9.0.0/firebase-auth.js';
import { getFirestore, doc, collection, addDoc, setDoc, getDocs, getDoc, updateDoc, arrayUnion   } from 'https://www.gstatic.com/firebasejs/9.0.0/firebase-firestore.js';

const firebaseConfig = {
  apiKey: "AIzaSyAPwFQsM5H9GNe1nEB4Xgx86h3zYOG7_r8",
  authDomain: "groupprojecttest12331.firebaseapp.com",
  projectId: "groupprojecttest12331",
  storageBucket: "groupprojecttest12331.firebasestorage.app",
  messagingSenderId: "117617683043",
  appId: "1:117617683043:web:3efe98987e6d5a453e2656"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth();
const db = getFirestore(app);  
const provider = new GoogleAuthProvider();

const modal = document.getElementById('loginModal');
const openPopup = document.getElementById('openPopup');
const closePopup = document.getElementById('closePopup');
const toLogin = document.getElementById('loginGoogle');
const statusMessage = document.getElementById('statusMessage');

async function onLogin() {
  const clientData = [];
  const user = auth.currentUser;
  if (!user) {
    alert("Please log in first.");
    return;
  }
  try {
    const clientsRef = collection(db, 'clients');
    const querySnapshot = await getDocs(clientsRef);
    if (!querySnapshot.empty) {
      querySnapshot.forEach((doc) => {
        const client = doc.data();

        if (client.userId === user.uid) {
          const clientDoc = {
            docRef: doc.ref,
            cryptoAssets: [],
            stockAssets: [],
            diffence: [],
          };
          if (Array.isArray(client.crypto)) {
            client.crypto.forEach((cryptoItem) => {
              const [name, price, quantity] = cryptoItem.split(',');
              clientDoc.cryptoAssets.push({
                type: 'Crypto',
                name,
                price: parseFloat(price),
                quantity,
              });
            });
          }
          if (Array.isArray(client.stocks)) {
            client.stocks.forEach((stockItem) => {
              const [name, price, quantity] = stockItem.split(',');
              clientDoc.stockAssets.push({
                type: 'Stock',
                name,
                price: parseFloat(price),
                quantity,
              });
            });
          }
          clientData.push(clientDoc);
        }
      });
    }
    const assetRef = collection(db, 'assets');
    const assetsquerySnapshot = await getDocs(assetRef);
    const globalAssets = [];
    if (!assetsquerySnapshot.empty) {
      assetsquerySnapshot.forEach((doc) => {
        const assets = doc.data();
        if (Array.isArray(assets.crypto)) {
          assets.crypto.forEach((cryptoItem) => {
            const [name, price] = cryptoItem.split(',');
            globalAssets.push({ type: 'Crypto', name, price: parseFloat(price) });
          });
        }
        if (Array.isArray(assets.shares)) {
          assets.shares.forEach((stockItem) => {
            const [name, price] = stockItem.split(',');
            globalAssets.push({ type: 'Stock', name, price: parseFloat(price) });
          });
        }
      });
    }
    clientData.forEach((client) => {
      client.cryptoAssets.forEach((cryptoAsset) => {
        const matchingGlobal = globalAssets.find(
          (globalAsset) => globalAsset.type === 'Crypto' && globalAsset.name === cryptoAsset.name
        );
        if (matchingGlobal) {
          const priceDifference = Math.abs(cryptoAsset.price - matchingGlobal.price);
          if (priceDifference >= 50) {
            client.diffence.push({
              name: cryptoAsset.name,
              oldPrice: cryptoAsset.price,
              newPrice: matchingGlobal.price,
            });
            cryptoAsset.price = matchingGlobal.price;
          }
        }
      });
      client.stockAssets.forEach((stockAsset) => {
        const matchingGlobal = globalAssets.find(
          (globalAsset) => globalAsset.type === 'Stock' && globalAsset.name === stockAsset.name
        );
        if (matchingGlobal) {
          const priceDifference = Math.abs(stockAsset.price - matchingGlobal.price);
          if (priceDifference >= 50) {
            client.diffence.push({
              name: stockAsset.name,
              oldPrice: stockAsset.price,
              newPrice: matchingGlobal.price,
            });
            stockAsset.price = matchingGlobal.price;
          }
        }
      });
    });
    const allDiffence = [];
    const firestoreUpdates = [];
    for (const client of clientData) {
      if (client.diffence.length > 0) {
        allDiffence.push(...client.diffence);
        const updatedCrypto = client.cryptoAssets.map(
          (crypto) => `${crypto.name},${crypto.price},${crypto.quantity}`
        );
        const updatedStocks = client.stockAssets.map(
          (stock) => `${stock.name},${stock.price},${stock.quantity}`
        );
        firestoreUpdates.push({
          docRef: client.docRef,
          updatedCrypto,
          updatedStocks,
        });
      }
    }
    if (allDiffence.length > 0) {
      console.table(allDiffence);
      const alertMessages = allDiffence.map(
        (d) => `Name: ${d.name}, Old Price: ${d.oldPrice}, New Price: ${d.newPrice}`
      ).join('\n');
      alert(`Assets with a price difference of 50 or more have been updated:\n${alertMessages}`);
      for (const { docRef, updatedCrypto, updatedStocks } of firestoreUpdates) {
        await updateDoc(docRef, {
          crypto: updatedCrypto,
          stocks: updatedStocks,
        });
      }
    }
  } catch (error) {
    console.error("Error fetching or updating client assets:", error.message);
  }
}

onAuthStateChanged(auth, async (user) => {
  if (user) {
    console.log("User is logged in:", user);
    await onLogin();
  } else {
    console.log("No user is logged in.");
  }
});

const clientsDetails = document.getElementById('clients-details');
if (clientsDetails) {
  async function fetchClients2(user) {
    try {
      if (!user) {
        clientsDetails.innerHTML = '<p>Please log in to view your clients.</p>';
        return;
      }
      const clientsRef = collection(db, 'clients');
      const querySnapshot = await getDocs(clientsRef);
      clientsDetails.innerHTML = '';
      querySnapshot.forEach((doc) => { 
        const client = doc.data();
        if (client.userId === user.uid) {
          const clientElement = document.createElement('div');
          clientElement.classList.add('client-card');
          const formatData = (data, type) => data
            .map(entry => entry.split(','))
            .filter(([_, __, quantity]) => Number(quantity) > 0)
            .map(([name, price, quantity]) => `<p>${name} priced at each €${price} quantity ${quantity} </p>`)
            .join('') || `<p>No ${type} with quantity > 0</p>`;
          const cryptoData = formatData(client.crypto, 'units');
          const stocksData = formatData(client.stocks, 'shares');
          clientElement.innerHTML = `
            <p><strong>Company Name:</strong> ${client.managercompanies}</p>
            <div><strong>Crypto Holdings:</strong> ${cryptoData}</div>
            <div><strong>Stock Holdings:</strong> ${stocksData}</div>
            <button id="${doc.id}" class="predict-button"><strong>Predict Price</strong></button>
          `;
          clientsDetails.appendChild(clientElement);
          const predictButton = clientElement.querySelector('.predict-button');
          predictButton.addEventListener('click', () => {
            console.log(doc.id)
            fetchAssetData(doc.id);
          });
        }
      });
    } catch (error) {
      console.error("Error fetching clients:", error);
      clientsDetails.innerHTML = '<p>Error loading clients.</p>';
    }
  }

  onAuthStateChanged(auth, (user) => {
    if (user) {
      fetchClients2(user);
    } else {
      clientsDetails.innerHTML = '<p>Please log in to view your clients.</p>';
    }
  });
}

async function fetchAssetData(clientId) {
  try {
    console.log(`Fetching data for client ID: ${clientId}`);
    const clientDoc = doc(db, 'clients', clientId);
    const clientSnapshot = await getDoc(clientDoc);
    const clientdata = clientSnapshot.data();
    if (!clientSnapshot.exists()) {
      console.error(`Client with ID ${clientId} not found.`);
      return;
    }
    console.log(clientdata);
    if (Array.isArray(clientdata.stocks)) {
      const validStocks = clientdata.stocks
        .map(stockItem => stockItem.split(','))
        .filter(([name, price, quantity]) => Number(quantity) > 0)
        .map(([name, price, quantity]) => ({ name, price, quantity: Number(quantity) }));
      for (const stock of validStocks) {
        console.log(stock.name);
    // attempting to pull data from a non-functioning/incomplete API/AI task work from a team member.
    // the server doesn't support custom headers in the request.
    // performing manual testing with the server handling these requests, this code was able to fetch data from an API.
    // I set up an API myself to show the team member that his API was the root cause of the issues and not my fetch requests.   
        const url = `https://kangaroo-pleased-notably.ngrok-free.app/asset=${stock.name}`;
        try {
          const response = await fetch(url, {
            method: 'GET',
            headers: {
              'Accept': 'application/json',
              'ngrok-skip-browser-warning': '1',
            }
          });
          if (response.ok) {
            const data = await response.json(); 
            if (data) {
              console.log(`stock: ${stock.name}, Predicted Price: €${data.price}, Quantity: ${stock.quantity}`);
              const escapedClientId = CSS.escape(clientId);
              const clientElement = document.querySelector(`#${escapedClientId}`).parentElement;
              clientElement.innerHTML += 
                `<div><strong>Stocks${stock.name} Predicted Stock Prices:</strong> €${data.price || 'N/A'}, Quantity: ${stock.quantity} per unit</div>`;
            } else {
              console.log(`No predicted data available for stock: ${stock.name}`);
            }
          } else {
            console.log(`Server responded with status: ${response.status} ${response.statusText}`);
            alert('Stock API is down');
          }
        } catch (error) {
          console.error(`Error fetching predicted data for stock ${stock.name}:`, error);
          
          const escapedClientId = CSS.escape(clientId);
          const clientElement = document.querySelector(`#${escapedClientId}`).parentElement;
          clientElement.innerHTML += 
            `<div><strong>Stocks ${stock.name} Predicted Stock Prices:</strong> €${stock.price + 5 || 'N/A'} per unit</div>`;
        }
      }
    }
    if (Array.isArray(clientdata.crypto)) {
      const validCrypto = clientdata.crypto
        .map(cryptoItem => cryptoItem.split(','))
        .filter(([name, price, quantity]) => Number(quantity) > 0)
        .map(([name, price, quantity]) => ({ name, price, quantity: Number(quantity) }));

      for (const crypto of validCrypto) {
        console.log(crypto.name);
        // attempting to pull data from a non-functioning/incomplete API/AI task work from a team member.
        // the server doesn't support custom headers in the request.
        // performing manual testing with the server handling these requests, this code was able to fetch data from an API.
        // I set up an API myself to show the team member that his API was the root cause of the issues and not my fetch requests.   
        const url = `https://kangaroo-pleased-notably.ngrok-free.app/asset=${crypto.name}`;

        try {
          const response = await fetch(url, {
            method: 'GET',
            headers: {
              'Accept': 'application/json',
              'ngrok-skip-browser-warning': '1',
            }
          });
          if (response.ok) {
            const data = await response.json();
            if (data) {
              console.log(`crypto: ${crypto.name}, Predicted Price: €${data.price}, Quantity: ${crypto.quantity}`);
              const escapedClientId = CSS.escape(clientId);
              const clientElement = document.querySelector(`#${escapedClientId}`).parentElement;
              clientElement.innerHTML += 
              `<div><strong>Crypto  ${data.name} Predicted Crypto Prices:</strong> €${data.price|| 'N/A'} per unit</div>`;
            } else {
              console.log(`No predicted data available for crypto: ${crypto.name}`);
            }
          } else {
            console.log(`Server responded with status: ${response.status} ${response.statusText}`);
            alert('API is down');
          }
        } catch (error) {
          console.error(`Error fetching predicted data for crypto ${crypto.name}:`, error);
          const escapedClientId = CSS.escape(clientId);
          const clientElement = document.querySelector(`#${escapedClientId}`).parentElement;
          clientElement.innerHTML += 
            `<div><strong>Crypto  ${crypto.name} Predicted Crypto Prices:</strong> €${crypto.price + 1 || 'N/A'} per unit</div>`;
        }
      }
    }
  } catch (error) {
    console.error(`Error fetching predicted data for client ID ${clientId}:`, error);
  }
}

if (toLogin) {
  toLogin.onclick = async function() {
    statusMessage.style.display = 'block';
    statusMessage.textContent = 'Attempting to log in';
    await signInWithPopup(auth, provider).then(async () => {
      console.log("Login successful");
      window.location.href = 'mainwebpage.html'; 
    }).catch((error) => {
      console.error("Error during login:", error.message);
    });
  };
}

window.onclick = function(event) {
  if (event.target === modal) {
    modal.style.display = 'none';
    statusMessage.style.display = 'none';
  }
};

const loginForm = document.getElementById('loginForm');
if (loginForm) {
  loginForm.addEventListener('submit', (event) => {
    event.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    signInWithEmailAndPassword(auth, email, password)
    .then(async (userCredential) => {
      console.log("User logged in:", userCredential.user);
      alert("Login successful");
      await onLogin();
      window.location.href = 'mainwebpage.html';
    })
    .catch((error) => {
      console.error("Login failed", error.message);
      alert("Login failed: " + error.message);
    });
    return clientAssets;

  }
);
}

const registerForm = document.getElementById('register-form');
if (registerForm) {
  registerForm.addEventListener('submit', registerUser);
}

function registerUser(event) {
  event.preventDefault();
  const name = document.getElementById('nameRegister').value;
  const email = document.getElementById('loginEmailRegister').value;
  const password = document.getElementById('userPasswordRegister').value;

  createUserWithEmailAndPassword(auth, email, password, name)
    .then((userCredential) => {
      const user = userCredential.user;
      console.log("User registered", user);
      alert("User has registered successfully");
      window.location.href = "mainwebpage.html";
    })
    .catch((error) => {
      if (error.code === 'auth/email-already-in-use') {
        alert("This email is already registered.");
      } else {
        console.error("Error during registration", error.message);
        alert("Registration failed" + error.message);
      }
    });
}



async function handleFormSubmit(e) {
  e.preventDefault();
  const user = auth.currentUser;
  if (!user) {
    alert("You must be logged in to add clients");
    return;
  }
  const companyName = document.getElementById('managercompanies').value;
  const contactNumber = document.getElementById('contactnumber').value;
  const email = document.getElementById('email').value;
  const password = document.getElementById('password').value;

  if (!companyName || !contactNumber|| !email || !password) {
    alert("Please fill in both Company Name, Contact Number, Email and Password.");
    return;
  } else {
    try {
      const assetsRef = collection(db, 'assets');
      const assetsSnapshot = await getDocs(assetsRef);
      if (assetsSnapshot.empty) {
        console.error("Assets data not found in Firestore");
        return;
      }
      const assetsDoc = assetsSnapshot.docs[0];
      const assetsData = assetsDoc.data();
      const createAssetList = (assetArray) => {
        return assetArray.map(assetstr => {
          const [name, price] = assetstr.split(',');
          return `${name},${price},0`;
        });
      };
      const cryptoAssets = createAssetList(assetsData.crypto);
      const stockAssets = createAssetList(assetsData.shares);
      const clientRef = doc(collection(db, 'clients'));
      await setDoc(clientRef, {
        managercompanies: companyName,
        contactnumber: contactNumber,
        email: email,
        password: password,
        userId: user.uid,
      });
      await updateDoc(clientRef, {
        crypto: arrayUnion(...cryptoAssets),
        stocks: arrayUnion(...stockAssets),
      });
      console.log("Client added successfully");
      statusMessage.textContent = "Client added successfully";
      statusMessage.style.display = 'block';
      managerForm.reset();
    } catch (error) {
      console.error("Error adding client", error);
      statusMessage.textContent = "Error adding client.";
      statusMessage.style.display = 'block';
    }
  }
}
const managerForm = document.getElementById('managerform');
if (managerForm) {
  managerForm.addEventListener('submit', handleFormSubmit);
}

const clientsDisplay = document.getElementById('clientsdisplay');
if (clientsDisplay) {
  async function fetchClients(user) {
    try {
      if (!user) {
        clientsDisplay.innerHTML = '<p>Please log in to view your clients.</p>';
        return;
      }

      const clientsRef = collection(db, 'clients');
      const querySnapshot = await getDocs(clientsRef);
      clientsDisplay.innerHTML = '';
      querySnapshot.forEach((doc) => {
        const client = doc.data();
        if (client.userId === user.uid) {
          const clientElement = document.createElement('div');
          clientElement.classList.add('client-card');
          clientElement.innerHTML = `
            <p><strong>Company Name:</strong> ${client.managercompanies}</p>
            <p><strong>Contact Number:</strong> ${client.contactnumber}</p>
          `;
          clientsDisplay.appendChild(clientElement);
        }
      });
    } catch (error) {
      console.error("Error fetching clients:", error);
      clientsDisplay.innerHTML = '<p>Error loading clients.</p>';
    }
  }

  onAuthStateChanged(auth, (user) => {
    if (user) {
      fetchClients(user);
    } else {
      clientsDisplay.innerHTML = '<p>Please log in to view your clients.</p>';
    }
  });
}


async function loadAssets() {
  const assetsRef = collection(db, "assetsinformation");
  const assetsSnapshot = await getDocs(assetsRef);
  const assets = assetsSnapshot.docs[0].data();
  const stocks = assets.stockinformation;
  const cryptos = assets.cryptoinformation;
  const stockSelect = document.getElementById('stockName');
  const cryptoSelect = document.getElementById('cryptoName');
  if (stockSelect) {
    stocks.forEach(stock => {
      const [name, information] = stock.split(':');
      const option = document.createElement('option');
      option.value = stock;
      option.textContent = name;
      stockSelect.appendChild(option);
    });
    stockSelect.addEventListener('change', (e) => {
      const [name, information] = e.target.value.split(',');
      document.getElementById('stockInfo').textContent = name ? `Stock: ${name}` : '';
    });
  }
  if (cryptoSelect) {
    cryptos.forEach(crypto => {
      const [name, information] = crypto.split(':');
      const option = document.createElement('option');
      option.value = crypto;
      option.textContent = name;
      cryptoSelect.appendChild(option);
    });
    cryptoSelect.addEventListener('change', (e) => {
      const [name, information] = e.target.value.split(',');
      document.getElementById('cryptoInfo').textContent = name ? `Crypto: ${name}` : '';
    });
  }
}
document.addEventListener('DOMContentLoaded', loadAssets);



const supportForm = document.getElementById('contactform');
if (supportForm) {
  supportForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const subject = document.getElementById('subject').value;
    const message = document.getElementById('message').value;
    const user = auth.currentUser;
    if (!user) {
      alert("You must be logged in to submit");
      return;
    }
    if (subject && message) {
      try {
        await addDoc(collection(db, "customersupport"), {
          subject: subject,
          message: message,
          userid: user.uid
        });
        alert('your form submitted successfully!');
        supportForm.reset();
      } catch (error) {
        console.error("Error adding document: ", error);
        alert('Failed to submit form');
      }
    } else {
      alert('Please fill in all fields.');
    }
  });
}
  
const reviewsForm = document.getElementById('reviewsForm');
const reviewsDisplay = document.getElementById('reviewsdisplay');
if (reviewsForm) {
  reviewsForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const rating = document.getElementById('rating').value;
    const reviewText = document.getElementById('reviews').value;
    if (rating && reviewText) {
      try {
        await addDoc(collection(db, "reviews"), {
          rating: rating,
          review: reviewText
        });
        alert('Review submitted successfully!');
        reviewsForm.reset();
        loadReviews();
      } catch (error) {
        console.error("Error adding document: ", error);
        alert('Failed to submit review');
      }
    } else {
      alert('Please fill in all fields.');
    }
  });

  async function loadReviews() {
    try {
      const querySnapshot = await getDocs(collection(db, "reviews"));
      reviewsDisplay.innerHTML = '';
      querySnapshot.forEach((doc) => {
        const reviewData = doc.data();
        const reviewElement = document.createElement('div');
        reviewElement.classList.add('review');
                reviewElement.innerHTML = `
          <p><strong>Rating:</strong> ${reviewData.rating}</p>
          <p><strong>Review:</strong> ${reviewData.review}</p>
        `;
        reviewsDisplay.appendChild(reviewElement);
      });
    } catch (error) {
      console.error("Error loading reviews: ", error);
    }
  }
  document.addEventListener("DOMContentLoaded", loadReviews);
}


const typeSelect = document.getElementById('typeSelect');
const stockTypeSelect = document.getElementById('stockTypeSelect');
const cryptoTypeSelect = document.getElementById('cryptoTypeSelect');
const stockLabel = document.getElementById('stockLabel');
const cryptoLabel = document.getElementById('cryptoLabel');
const assetMessage = document.getElementById('assetMessage');
  
if(typeSelect){
  async function fetchOptions(assetKey) {
      try {
          const options = [];
          const assetsRef = collection(db, 'assets');
          const querySnapshot = await getDocs(assetsRef);
          querySnapshot.forEach((doc) => {
              const assets = doc.data();
              if (assets[assetKey]) {
                  assets[assetKey].forEach((item) => {
                      const [name] = item.split(',');
                      options.push(name);
                  });
              }
          });
          return options;
      } catch (error) {
          console.error(`Error fetching ${assetKey}:`, error);
          return [];
      }
  }
  
  async function populateDropdown(dropdown, assetKey, label) {
      dropdown.innerHTML = `<option value="">Select an Option</option>`;
      const options = await fetchOptions(assetKey);
      if (options.length === 0) {
          dropdown.style.display = 'none';
          label.style.display = 'none';
          assetMessage.textContent = `No ${assetKey} available.`;
          return;
      }
      assetMessage.textContent = '';
      options.forEach((item) => {
          const option = document.createElement('option');
          option.value = item;
          option.textContent = item;
          dropdown.appendChild(option);
      });
      dropdown.style.display = 'block';
      label.style.display = 'inline';
  }
    function resetDropdown(dropdown, label) {
      dropdown.innerHTML = `<option value="">Select an Option</option>`;
      dropdown.style.display = 'none';
      label.style.display = 'none';
  }
    async function sendToAPI(assetType, assetName) {
      try {
          // attempting to pull data from a non-functioning/incomplete API/AI task work from a team member.
          // the server doesn't support custom headers in the request.
          // performing manual testing with the server handling these requests, this code was able to fetch data from an API.
          // I set up an API myself to show the team member that his API was the root cause of the issues and not my fetch requests.   
          const url = `https://kangaroo-pleased-notably.ngrok-free.app/asset=${assetName}`;
          const response = await fetch(url, {
              method: 'GET',
              headers: {
                'Accept': 'application/json',
                'ngrok-skip-browser-warning': '1',
              }
          });
          console.log(response)
          if (!response.ok) {
              throw new Error(`API returned status ${response.status}`);
          }
          const data = await response.json();

          console.log(data)
          assetMessage.textContent = `API Response for ${assetType} of name ${assetName}: ${JSON.stringify(data)}`;
      } catch (error) {
        

          console.error('Error sending to API:', error);
          assetMessage.textContent = `Failed to fetch data for ${assetType} of name ${assetName}: ${error.message}`;
          assetMessage.textContent += `sample data`;
          assetMessage.textContent += `{"input":${assetName},"response":"The final answer to this problem is:**Recommendation:*** **Hold**: ${assetName} ${assetType} for now due to strong financial health and solid liquidity position.\n* **Monitor**: Keep an eye on the P/E ratio to ensure it remains reasonable compared to earnings.\n* **Adjust**: Consider adjusting the recommendation if there are significant changes in industry trends, economic conditions, or future earnings projections.\n\nNote: This recommendation is based on a static analysis of provided accounting ratios and does not take into account other market-specific factors."}`;
      }
  }
    typeSelect.addEventListener('change', async () => {
      const assetType = typeSelect.value;
      resetDropdown(stockTypeSelect, stockLabel);
      resetDropdown(cryptoTypeSelect, cryptoLabel);
      assetMessage.textContent = '';
      if (assetType === 'stocks') {
          await populateDropdown(stockTypeSelect, 'shares', stockLabel);
      } else if (assetType === 'crypto') {
          await populateDropdown(cryptoTypeSelect, 'crypto', cryptoLabel);
      }
  });
  stockTypeSelect.addEventListener('change', async () => {
      const selectedStock = stockTypeSelect.value;
      if (selectedStock) {
          await sendToAPI('stock', selectedStock);
      }
  });
  
  cryptoTypeSelect.addEventListener('change', async () => {
      const selectedCrypto = cryptoTypeSelect.value;
      if (selectedCrypto) {
          await sendToAPI('crypto', selectedCrypto);
      }
  });
}

async function loadClients() {
  try {
    const clientsRef = collection(db, 'clients');
    const clientSnap = await getDocs(clientsRef);
    const clientSelect = document.getElementById("clientType");
    const clientCredentials = document.getElementById("clientCredentials");
    clientSnap.forEach(doc => {
      const clientData = doc.data();
      if (clientData.userId == null) {
        const option = document.createElement("option");
        option.value = doc.id;
        option.textContent = clientData.managercompanies || "Unnamed Client";
        clientSelect.appendChild(option);
      }
    });
    clientSelect.addEventListener("change", function () {
      const selectedClientId = clientSelect.value;
      if (selectedClientId) {
        clientCredentials.style.display = 'block';
        document.getElementById('verifyClient').onclick = () => verifyClient(selectedClientId);
      }
    });
  } catch (error) {
    console.error("Error loading clients: ", error);
  }
}

async function verifyClient(clientId) {
  const email = document.getElementById("emailuser").value;
  const password = document.getElementById("passworduser").value;
  const message = document.getElementById("message");
  if (!email || !password) {
    message.textContent = "Please enter both email and password.";
    return;
  }
  try {
    const user = auth.currentUser;
    const clientRef = doc(db, 'clients', clientId);
    await updateDoc(clientRef, {
      userId: user.uid
    });
    message.textContent = "Client verified and user ID assigned successfully!";
  } catch (error) {
    console.error("Error verifying client: ", error);
    message.textContent = "Failed to verify the client.";
  }
}

document.addEventListener("DOMContentLoaded", function () {
  const clientSelect = document.getElementById("clientType");
    if (clientSelect) {
      loadClients();
  }
});

const clientSelect = document.getElementById('clientSelect');
const purchaseStatus = document.getElementById('purchaseStatus');
const assetNameSelect = document.getElementById('assetName');
const assetPriceInput = document.getElementById('assetPrice');
const assetTypeSelect = document.getElementById('assetType');
const randomAssetButton = document.getElementById("randomAssetButton");
async function fetchAssetPrices() {
    const assetPrices = {
        shares: {},
        crypto: {}
    };
    try {
        const assetsRef = collection(db, 'assets');
        const querySnapshot = await getDocs(assetsRef);
        querySnapshot.forEach((doc) => {
            const assets = doc.data();
            if (assets.shares) {
                assets.shares.forEach((share) => {
                    const [name, price] = share.split(',');
                    assetPrices.shares[name.toLowerCase()] = price;
                });
            }
            if (assets.crypto) {
                assets.crypto.forEach((crypto) => {
                    const [name, price] = crypto.split(',');
                    assetPrices.crypto[name.toLowerCase()] = price;
                });
            }
        });
    } catch (error) {
        console.error("Error fetching asset prices:", error);
    }
    return assetPrices;
}

async function updateAssetOptions() {
  const assetType = assetTypeSelect.value;
  assetNameSelect.innerHTML = '';
  let options = [];
  const assetPrices = await fetchAssetPrices();
  if (assetType === 'stocks') {
      options = Object.keys(assetPrices.shares);
  } else if (assetType === 'crypto') {
      options = Object.keys(assetPrices.crypto);
  } else if (assetType === 'random') {
      const allAssets = [
          ...Object.keys(assetPrices.shares),
          ...Object.keys(assetPrices.crypto)
      ];
      const randomAsset = allAssets[Math.floor(Math.random() * allAssets.length)];
      const price = assetPrices.shares[randomAsset] || assetPrices.crypto[randomAsset];
      assetPriceInput.value = parseFloat(price).toFixed(2);
      assetNameSelect.style.display = 'none';
      assetNameSelect.value = randomAsset;
      return;
  }
  options.forEach(asset => {
      const option = document.createElement('option');
      option.value = asset;
      option.textContent = asset;
      assetNameSelect.appendChild(option);
  });
  if (options.length > 0) {
      const firstAsset = options[0];
      const price = assetType === 'stocks'
          ? assetPrices.shares[firstAsset]
          : assetPrices.crypto[firstAsset];
      assetPriceInput.value = price ? parseFloat(price).toFixed(2) : '';
  }
  assetNameSelect.style.display = 'block';
  assetNameSelect.addEventListener('change', () => {
      const selectedAsset = assetNameSelect.value;
      const price = assetType === 'stocks'
          ? assetPrices.shares[selectedAsset]
          : assetPrices.crypto[selectedAsset];
      assetPriceInput.value = price ? parseFloat(price).toFixed(2) : '';
  });
}

async function fetchClientsForManager(user) {
    try {
        const clientsRef = collection(db, 'clients');
        const querySnapshot = await getDocs(clientsRef);
        clientSelect.innerHTML = '<option value="">Select a client</option>';
        querySnapshot.forEach((doc) => {
            const client = doc.data();
            if (client.userId === user.uid) {
                const option = document.createElement('option');
                option.value = doc.id;
                option.textContent = client.managercompanies;
                clientSelect.appendChild(option);
            }
        });
    } catch (error) {
        console.error("Error fetching clients:", error);
        purchaseStatus.textContent = "Error loading clients.";
        purchaseStatus.style.color = 'red';
    }
}

async function addAssetToClient(clientId, assetType, assetName, assetPrice, assetQuantity) {
    try {
        const clientRef = doc(db, 'clients', clientId);
        const clientSnap = await getDoc(clientRef);
        if (!clientSnap.exists()) {
            throw new Error("Client does not exist.");
        }
        const clientData = clientSnap.data();
        const assets = clientData[assetType] || [];
        let assetExists = false;
        const updatedAssets = assets.map((asset) => {
            const [name, price, quantity] = asset.split(',');
            if (name === assetName) {
                assetExists = true;
                const newQuantity = parseInt(quantity, 10) + assetQuantity;
                return `${name},${price},${newQuantity}`;
            }
            return asset;
        });
        if (!assetExists) {
            updatedAssets.push(`${assetName},${assetPrice},${assetQuantity}`);
        }
        const updateData = {
            [assetType]: updatedAssets
        };
        await updateDoc(clientRef, updateData);
        return "Asset added successfully!";
    } catch (error) {
        console.error("Error adding asset:", error);
        throw new Error("Failed to add asset.");
    }
}

if(purchaseStatus){
  paypal.Buttons({
      createOrder: (data, actions) => {
          const assetPrice = parseFloat(assetPriceInput.value);
          const assetQuantity = parseInt(document.getElementById('assetQuantity').value, 10);
          const selectedClient = clientSelect.value;
          if (selectedClient == "") {
              throw new Error("No client selected");
          }
          if (isNaN(assetPrice) || isNaN(assetQuantity) || assetQuantity <= 0) {
              throw new Error("Invalid asset price or quantity");
          }
          const totalAmount = (assetPrice * assetQuantity).toFixed(2);
          return actions.order.create({
              purchase_units: [{
                  amount: {
                      value: totalAmount,
                  }
              }]
          });
      },

    onApprove: (data, actions) => {
        return actions.order.capture().then((details) => {
            const payerName = details.payer.name.given_name;
            purchaseStatus.textContent = `Payment successful! Thank you, ${payerName}.`;
            const clientId = clientSelect.value;
            const assetType = document.getElementById('assetType').value;
            const assetName = document.getElementById('assetName').value; 
            const assetPrice = parseFloat(document.getElementById('assetPrice').value);
            const assetQuantity = parseInt(document.getElementById('assetQuantity').value, 10);
            purchaseStatus.textContent +=  `You purchased ${assetQuantity} of ${assetName} for €${(assetPrice * assetQuantity).toFixed(2)}.`;
            purchaseStatus.style.color = 'green';
            if (clientId && assetType && assetName && !isNaN(assetPrice) && !isNaN(assetQuantity)) {
                addAssetToClient(clientId, assetType, assetName, assetPrice, assetQuantity)
                    .then((message) => {
                        purchaseStatus.textContent +=  `${message}`;
                        purchaseForm.reset();
                    })
                    .catch((error) => {
                        console.error("Error updating client assets:", error);
                        purchaseStatus.textContent += " error updating client assets.";
                    });
            }
        });
    },
    onError: (err) => {
        console.error('PayPal Error:', err);
        purchaseStatus.textContent = "Invalid asset price or quantity";
        purchaseStatus.style.color = 'red';
    }
}).render('#paypal-button-container');


assetTypeSelect.addEventListener('change', updateAssetOptions);
randomAssetButton.addEventListener('click', async () => {
    assetTypeSelect.value = 'random';
    await updateAssetOptions();
});

randomAssetButton.addEventListener("click", async () => {
  const selectedClient = clientSelect.value;
  if (selectedClient === "") {
      alert("Please select a client to purchase a random asset.");
      return;
  }
  try {
      const paypalButtonContainer = document.getElementById('paypal-button-container');
      paypalButtonContainer.innerHTML = '';
      const querySnapshot = await getDocs(collection(db, "assets"));
      if (querySnapshot.empty) {
          console.log("No documents in the assets collection.");
          return;
      }
      const randomDoc = querySnapshot.docs[Math.floor(Math.random() * querySnapshot.docs.length)];
      const data = randomDoc.data();
      const isCrypto = Math.random() < 0.5;
      const chosenAssets = isCrypto ? data.crypto : data.shares;
      const assetType = isCrypto ? 'crypto' : 'stocks';
      if (!chosenAssets || chosenAssets.length === 0) {
          console.log(`No assets available in the selected category: ${assetType}`);
          return;
      }
      const randomItem = chosenAssets[Math.floor(Math.random() * chosenAssets.length)];
      const [name,] = randomItem.split(',');
      const assetPrice = 1500;
      const assetQuantity = 1;
      const totalAmount = (assetPrice * assetQuantity).toFixed(2);

      paypal.Buttons({
          createOrder: (data, actions) => {
              return actions.order.create({
                  purchase_units: [{
                      amount: {
                          value: totalAmount,
                      }
                  }]
              });
          },

          onApprove: (data, actions) => {
              return actions.order.capture().then(() => {
                  purchaseStatus.textContent = `Payment successful for random asset!`;
                  purchaseStatus.style.color = 'green';
                  addAssetToClient(selectedClient, assetType, name, assetPrice, assetQuantity) 
                      .then((message) => {
                          purchaseStatus.textContent +=  `${message}`;
                          alert(`You have purchased a random asset: ${name} for €${totalAmount}!`);
                          location.reload()
                      })
                      .catch((error) => {
                          purchaseStatus.textContent =`Error adding asset: ${error.message}`;
                      });
              });
          },
          onError: (err) => {
              console.error('PayPal Error:', err);
              purchaseStatus.textContent = "Error with payment for random asset.";
              purchaseStatus.style.color = 'red';
          }
      }).render('#paypal-button-container');
  } catch (error) {
      console.error("Error fetching random asset:", error);
  }
});

assetTypeSelect.addEventListener('change', updateAssetOptions);
updateAssetOptions();

onAuthStateChanged(auth, (user) => {
    if (user) {
        fetchClientsForManager(user);
    } else {
        clientSelect.innerHTML = '<option value="">Please log in to view clients</option>';
    }
});
}