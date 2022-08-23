describe('Navigation', () => {
  it('redirects "/" to login', () => {
    cy.visit('/')

    // Should be redirected to login
    cy.url().should('include', '/login')
  })

  it('pages requiring authentication take users to login', () => {
    cy.visit('/users')

    // Should be redirected to login
    cy.url().should('include', '/login')
  })

  it('provides a way to register from the login page', () => {
    cy.visit('/login')
    cy.contains('Create an Account').click()

    // Should be redirected to register
    cy.url().should('include', '/register')
  })
})

describe('I can log into an existing account', () => {
  it('shows typed data', () => {
    cy.visit('/login')

    cy.get('[data-cy="username"]')
      .type('example_username')
      .should('have.value', 'example_username')
    cy.get('[data-cy="password"]')
      .type('example_password')
      .should('have.value', 'example_password')
  })

  it('redirects to profile page on successful login', () => {
    cy.visit('/login')

    // This will be reliant on your user database
    cy.get('[data-cy="username"]').type('cypress_test_username')
    cy.get('[data-cy="password"]').type('cypress_test_password')
    cy.get('[data-cy="login-button"]').click()

    // Should be redirected to profile page
    cy.url().should('match', /users\/\d*/)
  })
})