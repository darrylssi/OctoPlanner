const { expect } = require("chai")
const { describe } = require("mocha")

describe('Navigation', () => {
  it('provides a way to return to the login page', () => {
    cy.visit('/register')
    cy.contains('Login here').click()

    // Should be redirected to login
    cy.url().should('include', '/login')
  })
})

describe('Setup', () => {
  it('does not already have test user', () => {
    cy.fixture('users/student.json').then((user) => {
      cy.task('queryDb', `SELECT COUNT(*) as "rowCount", id FROM USERS WHERE username=` + user.username).then((result) => {
        if(result[0].rowCount != 0) {
          cy.task('queryDb', `DELETE FROM USER_ROLES WHERE user_id=` + result[0].id).then((resRoles) => {
            expect(resRoles.changedRows).to.greaterThanOrEqual(1)
          })
          cy.task('queryDb', `DELETE FROM USERS WHERE id=` + result[0].id).then((resUsers) => {
            expect(resUsers.changedRows).to.equal(1)
          })
        }
      })
    })
    })
})

describe('I can register a user', () => {
  it('shows typed data', () => {
    cy.fixture('users/student.json').then((user) => {
      cy.visit('/register')

      cy.get('[data-cy="firstName"]')
        .type(user.fname)
        .should('have.value', user.fname)
      cy.get('[data-cy="middleName"]')
        .type(user.mname)
        .should('have.value', user.mname)
      cy.get('[data-cy="lastName"]')
        .type(user.lname)
        .should('have.value', user.lname)
      cy.get('[data-cy="nickname"]')
        .type(user.nickname)
        .should('have.value', user.nickname)
      cy.get('[data-cy="username"]')
        .type(user.username)
        .should('have.value', user.username)
      cy.get('[data-cy="email"]')
        .type(user.email)
        .should('have.value', user.email)
      cy.get('[data-cy="pronouns"]')
        .type(user.pronouns)
        .should('have.value', user.pronouns)
      cy.get('[data-cy="bio"]')
        .type(user.bio)
        .should('have.value', user.bio)
      cy.get('[data-cy="password"]')
        .type(user.password)
        .should('have.value', user.password)
      cy.get('[data-cy="passwordConfirm"]')
        .type(user.password)
        .should('have.value', user.password)
    })
  })

  it('adds user to database and redirects to profile page on successful register', () => {
    cy.fixture('users/student.json').then((user) => {
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

      // Should now exist in the db
      cy.task('queryDb', `SELECT COUNT(*) as "rowCount" FROM USERS WHERE username=` + user.username).then((result) => {
        expect(result[0].rowCount).to.equal(1)
      })
      // Should be redirected to profile page
      cy.url().should('match', /users\/\d*/)
    })
  })
})