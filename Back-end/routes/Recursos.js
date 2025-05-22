// src/routes/servicios.js
const express             = require('express');
const router              = express.Router();
const auth                = require('../middleware/authMiddleware');
const C                   = require('../controllers/recursocontroller');

// Lecturas públicas
router.get('/',            (req,res,next) => C.list(req,res,next));
router.get('/categoria/:id',(req,res,next)=> C.byCategoria(req,res,next));
router.get('/gratuitos',   (req,res,next) => C.gratuitos(req,res,next));
router.get('/accesibles',  (req,res,next) => C.accesibles(req,res,next));
router.get('/:id',         (req,res,next) => C.getOne(req,res,next));

// Escrituras protegidas
router.post('/',    auth, (req,res,next) => C.create(req,res,next));
router.put('/:id',  auth, (req,res,next) => C.update(req,res,next));
router.delete('/:id', auth,(req,res,next)=> C.delete(req,res,next));

module.exports = router;
