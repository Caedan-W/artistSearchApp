const jwt = require('jsonwebtoken');

/**
 * 中间件：验证用户身份
 * 支持两种方式获取 Token：
 * 1) HTTP Cookie（req.cookies.jwt）
 * 2) Authorization: Bearer <token> 请求头
 */
function authenticateUser(req, res, next) {
    // 1. 尝试从 Cookie 中获取 JWT
    let token = req.cookies && req.cookies.jwt;

    // 2. 如果 Cookie 中没有，再尝试从 Authorization 头中获取
    if (!token && req.headers.authorization) {
        const authHeader = req.headers.authorization;
        if (authHeader.startsWith('Bearer ')) {
            token = authHeader.split(' ')[1];
        }
    }

    // 3. 如果仍然没有 Token，则拒绝访问
    if (!token) {
        return res.status(401).json({ error: 'Unauthorized' });
    }

    try {
        // 验证并解码 JWT
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.user = decoded;      // 将用户信息附加到请求对象
        next();                 // 继续后续中间件或路由处理
    } catch (error) {
        // 验证失败或过期
        return res.status(401).json({ error: 'Authentication failed' });
    }
}

module.exports = { authenticateUser };
