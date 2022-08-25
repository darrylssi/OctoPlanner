// For connecting to SQL Server
const mysql = require('mysql');
import { defineConfig } from "cypress";

// Sends a query to the database
function queryTestDb(query, config) {
  // Creates a new mysql connection using credentials from cypress.json env's
  const connection = mysql.createConnection(config.env.db)
  // Start connection to db
  connection.connect()
  // Exec query + disconnect to db as a Promise
  return new Promise((resolve, reject) => {
    connection.query(query, (error, results) => {
      if (error) reject(error)
      else {
        connection.end()
        return resolve(results)
      }
    })
  })
}

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:9000',
    supportFile: false,
    setupNodeEvents(on, config) {
      on('task', {
        queryDb: query => {
          return queryTestDb(query, config)
        },
      });
    }
  },
});
