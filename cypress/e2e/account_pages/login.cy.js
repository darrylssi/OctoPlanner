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

describe('Setup', () => {
  it('already has the test user', () => {
    cy.fixture('users/student.json').then((user) => {
      cy.task('queryDb', `SELECT COUNT(*) as "rowCount" FROM USERS WHERE username=` + user.username).then((result) => {
        if(result[0].rowCount != 1) {
          cy.visit('/register')

          cy.get('[data-cy="firstName"]').type(user.fname)
          cy.get('[data-cy="middleName"]').type(user.mname)
          cy.get('[data-cy="lastName"]').type(user.lname)
          cy.get('[data-cy="nickname"]').type(user.nickname)
          cy.get('[data-cy="username"]').type(user.username)
          cy.get('[data-cy="email"]').type(user.email)
          cy.get('[data-cy="pronouns"]').type(user.pronouns)
          cy.get('[data-cy="bio"]').type(user.bio)
          cy.get('[data-cy="password"]').type(user.password)
          cy.get('[data-cy="passwordConfirm"]').type(user.password)

          cy.get('[data-cy="register-button"]').click()

          // Should be redirected to profile page
          cy.url().should('match', /users\/\d*/)
        }
      })
    })
  })

  it('is logged out', () => {
    cy.getCookie('lens-session-token').should('not.exist')
  })
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