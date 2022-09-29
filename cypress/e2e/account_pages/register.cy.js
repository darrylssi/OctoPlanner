describe('Navigation', () => {
  it('provides a way to return to the login page', () => {
    cy.visit('/register')
    cy.contains('Login here').click()

    // Should be redirected to login
    cy.url().should('include', '/login')
  })
})

describe('I can register a student', () => {
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

  // If this test is failing, make sure that the user doesn't already exist in the database
  it('adds student to database and redirects to profile page on successful register', () => {
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

      // Ideally we would check here that the user is now in the database
      // Should be redirected to profile page
      cy.url().should('match', /users\/\d*/)
    })
  })
})

describe('I can register a teacher', () => {
  // If this test is failing, make sure that the user doesn't already exist in the database
  // Note that this doesn'tt make the user a teacher, you'll have to go and do that manually
  it('adds teacher to database and redirects to profile page on successful register', () => {
    cy.fixture('users/teacher.json').then((user) => {
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

      // Ideally we would check here that the user is now in the database
      // Should be redirected to profile page
      cy.url().should('match', /users\/\d*/)
    })
  })
})