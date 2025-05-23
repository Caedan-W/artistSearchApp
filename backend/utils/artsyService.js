const axios = require('axios');
const { getArtsyToken } = require('./artsyToken'); // 假设 Token 工具在此
const { BASE_URL } = require('../config/artsy'); // Artsy API 基础 URL

async function getArtsyArtistDetails(artistId) {
  if (!artistId) return null;
  try {
    const token = await getArtsyToken();
    const response = await axios.get(`${BASE_URL}/artists/${artistId}`, {
      headers: { 'X-XAPP-Token': token }
    });
    const artist = response.data;
    // 返回一个包含所需字段的对象
    return {
      // id: artist.id, // 可能不需要重复返回 ID
      // name: artist.name, // 可能不需要重复返回名字
      nationality: artist.nationality || null, // 确保返回 null 而不是 "" 或 "Unknown"
      birthday: artist.birthday || null,
      deathday: artist.deathday || null,
      imageUrl: artist._links?.thumbnail?.href || null, // 获取图片 URL
      // 根据需要添加其他字段
    };
  } catch (error) {
    // 如果是 404 错误，说明艺术家不存在，可以安全地返回 null
    if (error.response && error.response.status === 404) {
      console.log(`Artsy API 未找到艺术家 ID: ${artistId}`);
      return null;
    }
    // 其他错误则向上抛出，让调用者处理
    console.error(`从 Artsy API 获取艺术家 ${artistId} 详情失败:`, error.message);
    throw error; // 或者返回 null，取决于您希望如何处理错误
  }
}

module.exports = { getArtsyArtistDetails };
