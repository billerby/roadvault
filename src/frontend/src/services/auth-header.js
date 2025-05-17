// services/auth-header.js

/**
 * Funktion som returnerar Authorization-header med JWT-token om användaren är inloggad
 * @returns {object} Headers som innehåller Authorization token eller tomt objekt
 */
export default function authHeader() {
  const token = localStorage.getItem('token');
  
  if (token) {
    // För Spring Boot backend:
    return { 'Authorization': 'Bearer ' + token };
  } else {
    return {};
  }
}
