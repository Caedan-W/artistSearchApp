// routes/artist.js
const express = require('express');
const router = express.Router();
const axios = require('axios');
const { getArtsyToken } = require('../utils/artsyToken');
const { BASE_URL } = require('../config/artsy');

/**
 * 将 Markdown 风格的链接 [Text](URL) 替换为 Text
 */
function stripMdLinks(str) {
  return str.replace(/\[([^\]]+)\]\([^)]+\)/g, '$1');
}


// Search artist
router.get('/search/:query', async (req, res) => {
  const query = req.params.query.trim();
  if (!query) {
    return res.status(400).json({ error: "Search query is required" });
  }
  try {
    const token = await getArtsyToken();
    const response = await axios.get(`${BASE_URL}/search`, {
      headers: { 'X-XAPP-Token': token },
      params: { q: query, size: 10, type: 'artist' }
    });
    const results = response.data._embedded.results.map(artist => ({
      id: artist._links.self.href.split('/').pop(),
      name: artist.title,
      image: artist._links.thumbnail?.href || null
    }));
    res.json({ artists: results });
  } catch (error) {
    console.error("Failed to search artists:", error.response?.data || error.message);
    res.status(500).json({ error: "Failed to fetch search results" });
  }
});

// Fetch artist details
router.get('/artist/:id', async (req, res) => {
  const artistId = req.params.id.trim();
  if (!artistId) {
    return res.status(400).json({ error: "Artist ID is required" });
  }
  try {
    const token = await getArtsyToken();
    const response = await axios.get(`${BASE_URL}/artists/${artistId}`, {
      headers: { 'X-XAPP-Token': token }
    });
    const artist = response.data;
    res.json({
      id: artist.id,
      name: artist.name || "Unknown Artist",
      birthday: artist.birthday || "",
      deathday: artist.deathday || "",
      nationality: artist.nationality || "Unknown",
      biography: artist.biography || "",
      image: artist._links?.thumbnail?.href || "/images/artsy_logo.svg"
    });
  } catch (error) {
    console.error("Failed to fetch artist details:", error.response?.data || error.message);
    res.status(500).json({ error: "Failed to fetch artist details" });
  }
});

// Fetch artist's artworks
router.get('/artist/:id/artworks', async (req, res) => {
  const artistId = req.params.id.trim();
  if (!artistId) {
    return res.status(400).json({ error: "Artist ID is required" });
  }
  try {
    const token = await getArtsyToken();
    const response = await axios.get(`${BASE_URL}/artworks`, {
      headers: { 'X-XAPP-Token': token },
      params: { artist_id: artistId, size: 10 }
    });
    const artworks = response.data._embedded.artworks.map(artwork => ({
      id: artwork.id,
      title: artwork.title || "Untitled",
      date: artwork.date || "Unknown",
      image: artwork._links?.thumbnail?.href || "/default-artwork.png"
    }));
    res.json({ artworks });
  } catch (error) {
    console.error("Failed to fetch artworks:", error.response?.data || error.message);
    res.status(500).json({ error: "Failed to fetch artworks" });
  }
});

// Fetch artist category
router.get('/artwork/:id/categories', async (req, res) => {
  const artworkId = req.params.id.trim();
  if (!artworkId) {
    return res.status(400).json({ error: "Artwork ID is required" });
  }
  try {
    const token = await getArtsyToken();
    const response = await axios.get(`${BASE_URL}/genes`, {
      headers: { 'X-XAPP-Token': token },
      params: { artwork_id: artworkId }
    });
    const categories = response.data._embedded.genes.map(category => ({
      id: category.id,
      name: category.name || "Unknown",
      image: category._links?.thumbnail?.href || null,
      // 只保留文字，不要 URL
      description: stripMdLinks(category.description || '')
    }));
    res.json({ categories });
  } catch (error) {
    console.error("Failed to fetch categories:", error.response?.data || error.message);
    res.status(500).json({ error: "Failed to fetch categories" });
  }
});

// Get similar artist
router.get('/artist/:id/similar', async (req, res) => {
  const artistId = req.params.id.trim();
  if (!artistId) {
    return res.status(400).json({ error: "Artist ID is required" });
  }
  try {
    const token = await getArtsyToken();
    const response = await axios.get(`${BASE_URL}/artists`, {
      headers: { 'X-XAPP-Token': token },
      params: { similar_to_artist_id: artistId }
    });
    const similarArtists = response.data._embedded.artists.map(artist => ({
      id: artist.id,
      name: artist.name,
      image: artist._links.thumbnail?.href || "/default-artist.png"
    }));
    res.json({ similar: similarArtists });
  } catch (error) {
    console.error("Failed to fetch similar artists:", error.response?.data || error.message);
    res.status(500).json({ error: "Failed to fetch similar artists" });
  }
});

module.exports = router;
