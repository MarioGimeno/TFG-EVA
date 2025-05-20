// src/repositories/RecursoRepository.js
const { pool } = require('../config/Pool');

class RecursoRepository {
    async findAll() {
        const { rows } = await pool.query(`
          SELECT * FROM recurso
          ORDER BY id
        `);
        return rows;
      }
    
      async findById(id) {
        const { rows } = await pool.query(
          `SELECT * FROM recurso WHERE id = $1`,
          [id]
        );
        return rows[0];
      }
    
      async findByCategoria(idCategoria) {
        const { rows } = await pool.query(
          `SELECT * FROM recurso WHERE id_categoria = $1 ORDER BY id`,
          [idCategoria]
        );
        return rows;
      }
    
      async findFiltered(filters) {
        const condiciones = [];
        const valores = [];
        let idx = 1;
        if (filters.gratuito !== undefined) {
          condiciones.push(`gratuito = $${idx++}`); valores.push(filters.gratuito);
        }
        if (filters.accesible !== undefined) {
          condiciones.push(`accesible = $${idx++}`); valores.push(filters.accesible);
        }
        const where = condiciones.length ? `WHERE ${condiciones.join(' AND ')}` : '';
        const { rows } = await pool.query(`
          SELECT * FROM recurso
          ${where}
          ORDER BY id
        `, valores);
        return rows;
      }
    
      async create(data) {
        const cols = [
          'id_entidad','id_categoria','imagen','email','telefono',
          'direccion','horario','servicio','descripcion',
          'requisitos','gratuito','web','accesible'
        ];
        const placeholders = cols.map((_, i) => `$${i+1}`).join(',');
        const values = cols.map(c => data[c]);
        const { rows } = await pool.query(`
          INSERT INTO recurso (${cols.join(',')})
          VALUES (${placeholders})
          RETURNING *
        `, values);
        return rows[0];
      }
    
      async update(id, data) {
        const cols = [
          'id_entidad','id_categoria','imagen','email','telefono',
          'direccion','horario','servicio','descripcion',
          'requisitos','gratuito','web','accesible'
        ];
        const sets = cols.map((c,i)=>`${c}=$${i+1}`).join(',');
        const values = cols.map(c => data[c]);
        values.push(id);
        const { rows } = await pool.query(`
          UPDATE recurso SET ${sets}
          WHERE id = $${cols.length+1}
          RETURNING *
        `, values);
        return rows[0];
      }
    
      async delete(id) {
        await pool.query(`DELETE FROM recurso WHERE id = $1`, [id]);
      }
    }
    

module.exports = new RecursoRepository();
