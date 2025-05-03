// services/emailService.js
const AWS = require('aws-sdk');
const nodemailer = require('nodemailer');

AWS.config.update({
  region: process.env.AWS_REGION,             // e.g. 'eu-west-1'
  accessKeyId: process.env.AWS_ACCESS_KEY_ID,
  secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY
});

// Usando transporte SES nativo de Nodemailer
const transporter = nodemailer.createTransport({
  SES: new AWS.SES({ apiVersion: '2010-12-01' })
});

async function sendEmail({ to, subject, text, html }) {
  return transporter.sendMail({
    from: process.env.SMTP_FROM, // debe estar verificado en SES
    to, subject, text, html
  });
}

module.exports = { sendEmail };
