// config/database.js
require('dotenv').config();
const mongoose = require('mongoose');
console.log('→ Using MONGO_URI:',process.env.MONGO_URI);

const connectDB = async () => {
  try {
    await mongoose.connect(process.env.MONGODB_URI, {
      useNewUrlParser: true,
      useUnifiedTopology: true
    });
    console.log(' Successfully connected to MongoDB');
  } catch (error) {
    console.error(' MongoDB connection failed', error);
    process.exit(1);
  }
};

module.exports = connectDB;
