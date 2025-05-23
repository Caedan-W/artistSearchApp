// routes/auth.js
const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const crypto = require('crypto');
const jwt = require('jsonwebtoken');

const User = require('../models/user');
const Favorite = require('../models/favorite');
const { authenticateUser } = require('../middleware/authMiddleware');

// Register new account
router.post('/auth/register', async (req, res) => {
  try {
    const { fullname, email, password } = req.body;
    if (!fullname || !email || !password) {
      return res.status(400).json({ error: "All fields are required" });
    }

    const existingUser = await User.findOne({ email });
    if (existingUser) {
      return res.status(400).json({ email: "Email already exists" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const md5Hash = crypto.createHash('md5')
                          .update(email.trim().toLowerCase())
                          .digest('hex');
    const profileImageUrl = `https://www.gravatar.com/avatar/${md5Hash}?d=identicon`;

    const newUser = new User({
      fullname,
      email,
      password: hashedPassword,
      profileImageUrl
    });
    await newUser.save();

    // Generate JWT
    const token = jwt.sign(
      { userId: newUser._id, email: newUser.email },
      process.env.JWT_SECRET,
      { expiresIn: "1h" }
    );

    // Set httpOnly cookie (optional)
    res.cookie("jwt", token, { httpOnly: true });

    // Return both user info and token in JSON
    res.json({
      message: "User registered successfully",
      user: {
        id: newUser._id,
        fullname: newUser.fullname,
        email: newUser.email,
        profileImageUrl: newUser.profileImageUrl
      },
      token
    });
  } catch (error) {
    res.status(500).json({ error: "Internal Server Error", details: error.message });
  }
});

// User login
router.post('/auth/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    const user = await User.findOne({ email });
    if (!user || !(await bcrypt.compare(password, user.password))) {
      return res.status(400).json({ password: "Username or password is incorrect." });
    }

    // Generate JWT
    const token = jwt.sign(
      { userId: user._id, email: user.email },
      process.env.JWT_SECRET,
      { expiresIn: "2h" }
    );

    // Set httpOnly cookie (optional)
    res.cookie("jwt", token, { httpOnly: true });

    // Return both user info and token in JSON
    res.json({
      user: {
        id: user._id,
        fullname: user.fullname,
        email: user.email,
        profileImageUrl: user.profileImageUrl
      },
      token
    });
  } catch (error) {
    res.status(500).json({ error: "Internal Server Error", details: error.message });
  }
});

// User logout
router.post('/auth/logout', (req, res) => {
  res.clearCookie("jwt");
  res.json({ message: "Logout successful" });
});

// Delete account
router.post('/auth/delete', authenticateUser, async (req, res) => {
  const userId = req.user.userId;
  await Favorite.deleteMany({ userId });
  await User.findByIdAndDelete(userId);
  res.clearCookie("jwt");
  res.json({ message: "Account deleted successfully" });
});

// Get current user info
router.get('/me', authenticateUser, async (req, res) => {
  try {
    const user = await User.findById(req.user.userId).select('-password');
    if (!user) {
      return res.status(401).json({ error: 'Unauthorized' });
    }
    res.json({
      id: user._id,
      fullname: user.fullname,
      email: user.email,
      profileImageUrl: user.profileImageUrl
    });
  } catch (error) {
    res.status(500).json({ error: "Internal Server Error" });
  }
});

module.exports = router;
