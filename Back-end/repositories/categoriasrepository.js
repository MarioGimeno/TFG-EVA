// repositories/categoriasRepository.js
const { pool } = require('../config/Pool');

class CategoriasRepository {
  async findAll() {
    const { rows } = await pool.query(`
      SELECT id_categoria, nombre, img_categoria
        FROM categoria
       ORDER BY id_categoria
    `);
    return rows;
  }

  async findById(id) {
    const { rows } = await pool.query(
      `SELECT * FROM categoria WHERE id_categoria = $1`,
      [id]
    );
    return rows[0];
  }
}

module.exports = new CategoriasRepository();
