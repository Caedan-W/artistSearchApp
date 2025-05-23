require('dotenv').config();
const express = require('express');
const bcrypt = require('bcryptjs');
const cookieParser = require('cookie-parser');
const path = require('path'); 

const app = express();
app.use(express.json());
app.use(cookieParser());
const PORT = process.env.PORT || 3000;


const authRoutes = require('./routes/auth');
const artistRoutes = require('./routes/artist');
const favoriteRoutes = require('./routes/favorite');
const connectDB = require('./config/database');
const { getArtsyToken } = require('./utils/artsyToken');

connectDB();

app.use('/api', authRoutes);
app.use('/api', artistRoutes);
app.use('/api', favoriteRoutes);

app.get('/health', (req, res) => {
  res.send('OK');
});

// Start the server
async function startServer() {
    try {
        await getArtsyToken();
        console.log("[Token] Initial Artsy token fetched successfully.");
    } catch (err) {
        console.warn("[Token] Failed to prefetch Artsy token at startup:", err.message);
    }
    app.listen(PORT, () => {
        console.log(`Server is running at http://localhost:${PORT}`);
    });
}

startServer();