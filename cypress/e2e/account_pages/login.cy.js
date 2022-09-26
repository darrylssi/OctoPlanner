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

beforeEach(() => {
  // Check the user is not logged in
  cy.getCookie('lens-session-token').should('not.exist')
})

describe('I can log into an existing account', () => {
  it('shows typed data', () => {
    cy.visit('/login')

    cy.get('[data-cy="username"]')
      .type('cy-username')
      .should('have.value', 'cy-username')
    cy.get('[data-cy="password"]')
      .type('cy-password')
      .should('have.value', 'cy-password')
  })

  it('redirects to profile page on successful login', () => {
    cy.fixture('users/student.json').then((user) => {
      cy.visit('/login')

      cy.get('[data-cy="username"]').type(user.username)
      cy.get('[data-cy="password"]').type(user.password)
      cy.get('[data-cy="login-button"]').click()

      // Should be redirected to profile page
      cy.url().should('match', /users\/\d*/)
    })
  })
})

describe('I cannot log into a nonexistent account', () => {
  it('stays on same page on failure', () => {
    cy.fixture('users/nonexistent_user.json').then((user) => {
      cy.visit('/login')

      cy.get('[data-cy="username"]').type(user.username)
      cy.get('[data-cy="password"]').type(user.password)
      cy.get('[data-cy="login-button"]').click()

      // Should be redirected to profile page
      cy.url().should('include', '/login')
    })
  })
})