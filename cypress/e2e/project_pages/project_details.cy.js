let cookiesCache = {}

function saveCookies() {
    cy.getCookies().then((cookies) => {
        cookies.forEach(({ name, secure, ...rest }) => {
            cookiesCache[name] = {
                name,
                secure: false,  // Manually set this to false in the test environment, otherwise cy.visit doesn't send it
                ...rest,
            };
        });
    });
}

function loadCookies() {
    Object.keys(cookiesCache).forEach((key) => {
        const { name, value, ...rest } = cookiesCache[key];

        cy.setCookie(name, value, rest);
    });
}

const login = (userfile) => {
  // Ideally this would work with cy.request, but that doesn't seem to want to happen now
  cy.visit('/login')
  cy.fixture(`users/${userfile}.json`).then((user) => {
    cy.get('[data-cy="username"]').type(user.username)
    cy.get('[data-cy="password"]').type(user.password)
    cy.get('[data-cy="login-button"]').click()
    cy.url().should('contain', '/users')
    saveCookies()
  })
}

describe('Navigation', () => {
  before(() => {
    login('student')
  })

  it('provides a way to access pages in the navigation pane', () => {
    loadCookies()
    cy.visit('/project/0')

    // Check we can get to list of users
    cy.get('[data-cy="navUsers"]').click()
    cy.url().should('include', '/users')

    // Check we can get to monthly calendar
    cy.get('[data-cy="navMonthlyCalendar"]').click()
    cy.url().should('include', '/monthlyCalendar')
  })
})

describe('Project details are shown to students', () => {
  before(() => {
    login('student')
  })

  it('shows the project name in the page title', () => {
    loadCookies()
    cy.visit('/project/0')
    // Test that it has the default project name
    cy.get('[data-cy="pageTitle"]')
      .should('have.text', "Project 2022")
  })
})