const fs = require('fs');
const { TMPDIR } = require('../config');

function ensureTmpDir() {
  if (!fs.existsSync(TMPDIR)) fs.mkdirSync(TMPDIR, { recursive: true });
}
module.exports = { ensureTmpDir };
