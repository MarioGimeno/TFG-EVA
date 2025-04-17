// src/services/db.js
const { pool } = require('../config');
module.exports = {
  query: (text, params) => pool.query(text, params)
};
