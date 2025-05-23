const fs = require('fs');
const axios = require('axios');
const path = require('path');

const ARTSY_API_BASE = "https://api.artsy.net/api";
const CLIENT_ID = process.env.ARTSY_CLIENT_ID;
const CLIENT_SECRET = process.env.ARTSY_CLIENT_SECRET;

const TOKEN_FILE = path.join(__dirname, '../artsy_token.json');


let ARTSY_TOKEN = null;
let TOKEN_EXPIRATION = 0;

function loadSavedToken() {
    if (fs.existsSync(TOKEN_FILE)) {
        const data = JSON.parse(fs.readFileSync(TOKEN_FILE, 'utf8'));
        if (data.token && data.expiration > Math.floor(Date.now() / 1000)) {
            ARTSY_TOKEN = data.token;
            TOKEN_EXPIRATION = data.expiration;
            console.log("Loaded saved Artsy API token from file.");
        }
    }
}

function saveToken(token, expiration) {
    fs.writeFileSync(TOKEN_FILE, JSON.stringify({ token, expiration }, null, 2));
    console.log("Token saved to file.");
}

async function getArtsyToken() {
    const currentTime = Math.floor(Date.now() / 1000);
    if (ARTSY_TOKEN && TOKEN_EXPIRATION > currentTime) {
        return ARTSY_TOKEN;
    }

    try {
        const response = await axios.post(`${ARTSY_API_BASE}/tokens/xapp_token`, {
            client_id: CLIENT_ID,
            client_secret: CLIENT_SECRET
        });
        ARTSY_TOKEN = response.data.token;
        TOKEN_EXPIRATION = Math.floor(new Date(response.data.expires_at).getTime() / 1000);
        saveToken(ARTSY_TOKEN, TOKEN_EXPIRATION);
        return ARTSY_TOKEN;
    } catch (error) {
        console.error("Failed to fetch Artsy API token:", error.message);
        throw new Error("Artsy API authentication failed");
    }
}

loadSavedToken();

module.exports = { getArtsyToken };
