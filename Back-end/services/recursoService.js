// services/recursoService.js
const { pool } = require('../config');

async function getAllRecursos() {
  const { rows } = await pool.query(`
    SELECT id, id_entidad, id_categoria,
           imagen, email, telefono, direccion, horario,
           servicio, descripcion, requisitos,
           gratuito, web, accesible
      FROM recurso
     ORDER BY id
  `);
  return rows;
}

async function getRecursoById(id) {
  const { rows } = await pool.query(
    `SELECT * FROM recurso WHERE id = $1`,
    [id]
  );
  return rows[0];
}

async function createRecurso(data) {
  const {
    id_entidad, id_categoria,
    imagen, email, telefono, direccion, horario,
    servicio, descripcion, requisitos,
    gratuito, web, accesible
  } = data;
  const { rows } = await pool.query(`
    INSERT INTO recurso
      (id_entidad, id_categoria, imagen, email, telefono,
       direccion, horario, servicio, descripcion,
       requisitos, gratuito, web, accesible)
    VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13)
    RETURNING *
  `, [
    id_entidad, id_categoria, imagen, email, telefono,
    direccion, horario, servicio, descripcion,
    requisitos, gratuito, web, accesible
  ]);
  return rows[0];
}

async function updateRecurso(id, data) {
  const {
    id_entidad, id_categoria,
    imagen, email, telefono, direccion, horario,
    servicio, descripcion, requisitos,
    gratuito, web, accesible
  } = data;
  const { rows } = await pool.query(`
    UPDATE recurso SET
      id_entidad   = $1,
      id_categoria = $2,
      imagen       = $3,
      email        = $4,
      telefono     = $5,
      direccion    = $6,
      horario      = $7,
      servicio     = $8,
      descripcion  = $9,
      requisitos   = $10,
      gratuito     = $11,
      web          = $12,
      accesible    = $13
    WHERE id = $14
    RETURNING *
  `, [
    id_entidad, id_categoria, imagen, email, telefono,
    direccion, horario, servicio, descripcion,
    requisitos, gratuito, web, accesible,
    id
  ]);
  return rows[0];
}

async function deleteRecurso(id) {
  await pool.query(
    `DELETE FROM recurso WHERE id = $1`,
    [id]
  );
}

module.exports = {
  getAllRecursos,
  getRecursoById,
  createRecurso,
  updateRecurso,
  deleteRecurso
};
