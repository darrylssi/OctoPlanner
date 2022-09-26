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

describe('I can add a valid sprint', () => {
  before(() => {
    login('teacher')
  })

  it('adds a sprint with default values', () => {
  loadCookies()
    cy.visit('/project/0')
    cy.contains('Add Sprint').click()
    cy.get('[data-cy="add-sprint-save"]').click()
    cy.contains('Sprint 1').should('be.visible')
  })

  it('adds a sprint with a changed name', () => {
  loadCookies()
    cy.visit('/project/0')
    cy.contains('Add Sprint').click()
    cy.get('[id="addSprintNameInput"]')
        .clear()
      .type('New Sprint')
    cy.get('[data-cy="add-sprint-save"]').click()
    cy.contains('New Sprint').should('be.visible')
  })

  it('adds a sprint with a changed description', () => {
  loadCookies()
    cy.visit('/project/0')
    cy.contains('Add Sprint').click()
    cy.get('[id="addSprintDescriptionInput"]')
        .clear()
      .type('This is a description')
    cy.get('[data-cy="add-sprint-save"]').click()
    cy.contains('This is a description').should('be.visible')
  })

  it('adds a sprint with changed dates', () => {
  loadCookies()
    cy.visit('/project/0')
    cy.contains('Add Sprint').click()
    cy.get('[id="sprintStartDate"]')
        .clear()
      .type('2023-04-10')
    cy.get('[id="sprintEndDate"]')
        .clear()
      .type('2023-04-20')
    cy.get('[data-cy="add-sprint-save"]').click()
    cy.contains('10/Apr/2023 - 20/Apr/2023').should('be.visible')
  })
})

describe("I can't add an invalid sprint", () => {

  it("doesn't add a sprint without a name", () => {
  loadCookies()
    cy.visit('/project/0')
    cy.contains('Add Sprint').click()
    cy.get('[id="addSprintNameInput"]')
       .clear()
      .type('  ')
    cy.get('[data-cy="add-sprint-save"]').click()
    cy.contains('Name cannot be blank').should('be.visible')
  })

  it("doesn't add a sprint with an invalid name", () => {
  loadCookies()
    cy.visit('/project/0')
    cy.contains('Add Sprint').click()
    cy.get('[id="addSprintNameInput"]')
       .clear()
      .type('<New Sprint>')
    cy.get('[data-cy="add-sprint-save"]').click()
    cy.contains(' Name can only have letters, numbers, punctuations except commas, and spaces.').should('be.visible')
  })

  it("doesn't add a sprint overlapping another sprint", () => {
  loadCookies()
    cy.visit('/project/0')
    cy.contains('Add Sprint').click()
    cy.get('[id="sprintStartDate"]')
        .clear()
      .type('2023-04-15')
    cy.get('[id="sprintEndDate"]')
        .clear()
      .type('2023-04-25')
    cy.get('[data-cy="add-sprint-save"]').click()
    cy.contains('Sprint dates must not overlap with other sprints. Dates are overlapping with 10/Apr/2023 - 20/Apr/2023').should('be.visible')
  })
})