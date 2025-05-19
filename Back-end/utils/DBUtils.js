// src/services/db.js
const { pool } = require('../config/Pool');
module.exports = {
  query: (text, params) => pool.query(text, params)
};
