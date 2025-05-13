// services/categoriaService.js
const { pool } = require('../config');

async function getAllCategorias() {
  const { rows } = await pool.query(`
    SELECT id_categoria, nombre
      FROM categoria
     ORDER BY id_categoria
  `);
  return rows;
}

async function getCategoriaById(id) {
  const { rows } = await pool.query(
    `SELECT * FROM categoria WHERE id_categoria = $1`,
    [id]
  );
  return rows[0];
}

module.exports = {
  getAllCategorias,
  getCategoriaById
};
