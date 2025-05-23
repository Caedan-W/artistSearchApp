const mongoose = require('mongoose');

const favoriteSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  artistId: String,
  artistName: String,
  artistImage: String,
  nationality: { type: String, default: null },
  birthday: { type: String, default: null },
  deathday: { type: String, default: null },
  addedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Favorite', favoriteSchema);
