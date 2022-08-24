describe('Navigation', () => {
  it('provides a way to return to the login page', () => {
    cy.visit('/register')
    cy.contains('Login here').click()

    // Should be redirected to login
    cy.url().should('include', '/login')
  })
})