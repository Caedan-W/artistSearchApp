const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  fullname: String,
  email: { type: String, unique: true },
  password: String,
  profileImageUrl: String
});

module.exports = mongoose.model('User', userSchema);
