// routes/favorite.js
const express = require('express');
const router = express.Router();
const { authenticateUser } = require('../middleware/authMiddleware'); // 身份验证中间件
const Favorite = require('../models/favorite'); // Mongoose 模型

// --- 导入获取 Artsy 详情的辅助函数 ---
const { getArtsyArtistDetails } = require('../utils/artsyService');

// === 获取用户收藏列表 ===
// (此路由保持不变)
router.get('/favorites', authenticateUser, async (req, res) => {
  try {
    // .find() 默认获取所有字段，所以如果数据库有信息，就会返回
    const favorites = await Favorite.find({ userId: req.user.userId }).sort({ addedAt: -1 });
    console.log(`用户 [${req.user.userId}] 获取到 ${favorites.length} 条收藏记录。`); // 添加日志
    res.json({ favorites });
  } catch (error) {
    console.error(`为用户 [${req.user.userId}] 获取收藏列表失败:`, error); // 添加错误日志
    res.status(500).json({ error: "Failed to fetch favorites" });
  }
});

// === 添加收藏 ===
// (此路由被修改以实现方法三)
router.post('/favorites', authenticateUser, async (req, res) => {
  try {
    // 1. 从请求体获取基础数据
    const { artistId, artistName, artistImage, nationality, birthday, deathday } = req.body;

    // 2. 基础验证
    if (!artistId || !artistName) { // ID 和 Name 是最核心的
      console.log(`添加收藏失败: 缺少 artistId 或 artistName。请求体:`, req.body); // 记录请求体
      return res.status(400).json({ error: "Required artist ID or Name missing" });
    }

    // 检查是否已存在 (保持不变)
    const existingFavorite = await Favorite.findOne({ userId: req.user.userId, artistId });
    if (existingFavorite) {
      console.log(`添加收藏失败: 艺术家 [${artistId}] 已在用户 [${req.user.userId}] 的收藏中。`);
      return res.status(400).json({ error: "Artist already in favorites" });
    }

    // --- 修改开始 (方法三: 检查并补充详情) ---
    let fetchedDetails = null; // 用于存储从 Artsy API 获取的详情

    // 3. 检查关键详情 (国籍或生日) 是否缺失
    //    我们认为如果请求体中没有提供有效的国籍或生日，就值得去尝试获取
    if (!nationality || !birthday) {
      console.log(`收藏请求 [${artistId}] 缺少详情 (国籍: ${nationality}, 生日: ${birthday})，尝试从 Artsy API 获取...`);
      try {
        // 4. 调用辅助函数获取 Artsy 详情 (需要您已实现 getArtsyArtistDetails)
        fetchedDetails = await getArtsyArtistDetails(artistId);
        if (fetchedDetails) {
          console.log(`成功从 Artsy API 获取到 [${artistId}] 的详情。`);
        } else {
          // 可能是 Artsy API 返回 404 或其他原因导致未获取到详情
          console.log(`未能从 Artsy API 获取到 [${artistId}] 的详情 (可能艺术家不存在或 API 问题)。将仅使用请求体中的信息。`);
        }
      } catch (fetchError) {
        // 5. 处理获取 Artsy 详情时发生的错误
        console.error(`尝试为 [${artistId}] 获取 Artsy 详情时发生错误:`, fetchError.message);
        // 在这种情况下，我们选择记录错误，但仍然继续尝试保存收藏（使用请求中已有的信息）
        // 您也可以选择在这里返回错误给客户端
        fetchedDetails = null; // 确保出错时 fetchedDetails 为 null
      }
    } else {
      console.log(`收藏请求 [${artistId}] 已包含国籍和生日，无需从 Artsy API 获取。`);
    }

    // 6. 准备最终要保存的数据 (合并数据源)
    //    规则：优先使用请求体中明确提供的有效值，否则尝试使用获取到的值，最后 fallback 为 null
    const dataToSave = {
      userId: req.user.userId,
      artistId: artistId,
      artistName: artistName, // 名字通常是必需的，来自请求
      // 图片处理：如果请求体提供了图片且不是默认图，用它；否则尝试用获取到的，最后为 null
      artistImage: (artistImage && artistImage !== "/images/artsy_logo.svg" && artistImage !== "/default-artist.png") ? artistImage : (fetchedDetails?.imageUrl || artistImage || null), // 保留请求中的图片作为最后的备选
      // 国籍：优先用请求体，其次用获取的，最后 null
      nationality: nationality || fetchedDetails?.nationality || null,
      // 生日：优先用请求体，其次用获取的，最后 null
      birthday: birthday || fetchedDetails?.birthday || null,
      // 卒日：优先用请求体，其次用获取的，最后 null
      deathday: deathday || fetchedDetails?.deathday || null,
      // 添加时间始终为当前时间
      addedAt: new Date()
    };
    console.log(`准备保存到数据库的数据 for [${artistId}]:`, dataToSave); // 记录最终保存的数据
    // --- 修改结束 ---

    // 7. 创建并保存 Favorite 文档
    const favorite = new Favorite(dataToSave); // 使用准备好的数据创建 Mongoose 模型实例
    await favorite.save(); // 保存到 MongoDB

    console.log(`用户 [${req.user.userId}] 成功添加艺术家 [${artistId}] 到收藏。`);

    // 8. 返回保存后的完整收藏对象
    //    使用 201 Created 状态码表示资源创建成功
    res.status(201).json({ favorite });

  } catch (error) {
    // 处理 Mongoose 保存错误或其他意外错误
    console.error(`为用户 [${req.user.userId}] 添加收藏 [${artistId || 'ID未知'}] 时发生内部错误:`, error);
    res.status(500).json({ error: "Failed to add favorite due to an internal error" });
  }
});

// === 删除收藏 ===
// (此路由保持不变)
router.delete('/favorites/:artistId', authenticateUser, async (req, res) => {
  try {
    const artistId = req.params.artistId;
    const result = await Favorite.deleteOne({ userId: req.user.userId, artistId }); // 使用 deleteOne

    if (result.deletedCount === 0) {
      // 如果没有文档被删除，可能表示该收藏不存在
      console.log(`尝试为用户 [${req.user.userId}] 删除收藏 [${artistId}]，但未找到该记录。`);
      return res.status(404).json({ message: "Favorite not found for this user." });
    }

    console.log(`用户 [${req.user.userId}] 成功从收藏中移除艺术家 [${artistId}]。`);
    res.json({ message: "Artist removed from favorites" }); // 返回成功消息

  } catch (error) {
    console.error(`为用户 [${req.user.userId}] 删除收藏 [${req.params.artistId}] 失败:`, error); // 记录错误
    res.status(500).json({ error: "Failed to remove favorite" });
  }
});

module.exports = router; // 导出路由