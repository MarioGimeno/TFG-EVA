// services/entidadService.js
const { pool } = require('../config');

async function getAllEntidades() {
  const { rows } = await pool.query(`
    SELECT id_entidad, imagen, email, telefono, pagina_web, direccion, horario
      FROM entidad
      ORDER BY id_entidad
  `);
  return rows;
}

async function getEntidadById(id) {
  const { rows } = await pool.query(
    `SELECT * FROM entidad WHERE id_entidad = $1`,
    [id]
  );
  return rows[0];
}

async function createEntidad(data) {
  const {
    imagen, email, telefono,
    pagina_web, direccion, horario
  } = data;
  const { rows } = await pool.query(`
    INSERT INTO entidad
      (imagen, email, telefono, pagina_web, direccion, horario)
    VALUES ($1,$2,$3,$4,$5,$6)
    RETURNING *
  `, [imagen, email, telefono, pagina_web, direccion, horario]);
  return rows[0];
}

async function updateEntidad(id, data) {
  const {
    imagen, email, telefono,
    pagina_web, direccion, horario
  } = data;
  const { rows } = await pool.query(`
    UPDATE entidad SET
      imagen = $1,
      email = $2,
      telefono = $3,
      pagina_web = $4,
      direccion = $5,
      horario = $6
    WHERE id_entidad = $7
    RETURNING *
  `, [imagen, email, telefono, pagina_web, direccion, horario, id]);
  return rows[0];
}

async function deleteEntidad(id) {
  await pool.query(
    `DELETE FROM entidad WHERE id_entidad = $1`,
    [id]
  );
}

module.exports = {
  getAllEntidades,
  getEntidadById,
  createEntidad,
  updateEntidad,
  deleteEntidad
};
