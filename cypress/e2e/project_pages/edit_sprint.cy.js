/*
 * These tests require a teacher user to be available. Please use the user defined in teacher.json and make sure they
 * have the teacher role.
 * These tests may fail if there are existing sprints in the database, and if the project dates don't include May 2023
 */

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

describe('I can edit a valid sprint', () => {
  before(() => {
    login('teacher')
  })

  it('adds first sprint with new values', () => {
    loadCookies()
      cy.visit('/project/0')
      cy.contains('Add Sprint').click()
      cy.get('[id="addSprintNameInput"]')
              .clear()
            .type('Sprint 1 name')
      cy.get('[id="addSprintDescriptionInput"]')
              .clear()
            .type('Sprint 1 description')
      cy.get('[id="sprintStartDate"]')
              .clear()
            .type('2023-04-10')
      cy.get('[id="sprintEndDate"]')
              .clear()
            .type('2023-04-20')
      cy.get('[data-cy="add-sprint-save"]').click()
      cy.contains('Sprint 1').should('be.visible')
    })

   it('adds second sprint with new values', () => {
   loadCookies()
     cy.visit('/project/0')
     cy.contains('Add Sprint').click()
     cy.get('[id="addSprintNameInput"]')
             .clear()
           .type('Sprint 2 name')
     cy.get('[id="addSprintDescriptionInput"]')
             .clear()
           .type('Sprint 2 description')
     cy.get('[id="sprintStartDate"]')
             .clear()
           .type('2023-05-04')
     cy.get('[id="sprintEndDate"]')
             .clear()
           .type('2023-05-20')
     cy.get('[data-cy="add-sprint-save"]').click()
     cy.contains('Sprint 2').should('be.visible')
   })

  it('edits a sprint with new values', () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[id="sprintEdit-1"]').click()
    cy.get('[id="sprintName"]')
            .clear()
          .type('This is a sprint')
    cy.get('[id="sprintDescription"]')
            .clear()
          .type('This is a description')
    cy.get('[id="sprintStartDate"]')
            .clear()
          .type('2023-04-10')
    cy.get('[id="sprintEndDate"]')
            .clear()
          .type('2023-04-20')
    cy.get('[data-cy="edit-sprint-save"]').click()
    cy.contains('This is a sprint').should('be.visible')
    cy.contains('This is a description').should('be.visible')
    cy.contains('10/Apr/2023 - 20/Apr/2023').should('be.visible')
  })

  it('edits a sprint with a changed name', () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[id="sprintEdit-1"]').click()
    cy.get('[id="sprintName"]')
        .clear()
      .type('This is a test sprint')
    cy.get('[data-cy="edit-sprint-save"]').click()
    cy.contains('This is a test sprint').should('be.visible')
  })

  it('edits a sprint with a changed description', () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[id="sprintEdit-1"]').click()
    cy.get('[id="sprintDescription"]')
        .clear()
      .type('This is a test description')
    cy.get('[data-cy="edit-sprint-save"]').click()
    cy.contains('This is a test description').should('be.visible')
  })

  it('edits a sprint with changed dates', () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[id="sprintEdit-1"]').click()
    cy.get('[id="sprintStartDate"]')
        .clear()
      .type('2023-04-20')
    cy.get('[id="sprintEndDate"]')
        .clear()
      .type('2023-05-05')
    cy.get('[data-cy="edit-sprint-save"]').click()
    cy.contains('20/Apr/2023 - 05/May/2023').should('be.visible')
  })
})

describe("I can't edit an invalid sprint", () => {

  it("doesn't edit a sprint without a name", () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[id="sprintEdit-1"]').click()
    cy.get('[id="sprintName"]')
       .clear()
      .type('  ')
    cy.get('[data-cy="edit-sprint-save"]').click()
    cy.contains('Name cannot be blank').should('be.visible')
  })

  it("doesn't edit a sprint with an invalid name", () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[id="sprintEdit-1"]').click()
    cy.get('[id="sprintName"]')
       .clear()
      .type('<New Sprint>')
    cy.get('[data-cy="edit-sprint-save"]').click()
    cy.contains(' Name can only have letters, numbers, punctuations except commas, and spaces.').should('be.visible')
  })

  it("doesn't edit a sprint with an invalid description", () => {
    loadCookies()
    cy.visit('/project/0')
    cy.get('[id="sprintEdit-1"]').click()
    cy.get('[id="sprintDescription"]')
        .clear()
        .type('😃')
    cy.get('[data-cy="edit-sprint-save"]').click()
    cy.contains('Description can only have letters, numbers, punctuations, and spaces.').should('be.visible')
  })

  it('edits a sprint with start date being more than end date', () => {
    loadCookies()
      cy.visit('/project/0')
      cy.get('[id="sprintEdit-1"]').click()
      cy.get('[id="sprintStartDate"]')
          .clear()
        .type('2023-04-10')
      cy.get('[id="sprintEndDate"]')
          .clear()
        .type('2023-04-05')
      cy.get('[data-cy="edit-sprint-save"]').click()
      cy.contains('Start date must always be before end date').should('be.visible')
    })

  it("doesn't edit a sprint overlapping another sprint", () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[id="sprintEdit-2"]').click()
    cy.get('[id="sprintName"]')
        .clear()
      .type('This is a sprint 2')
    cy.get('[id="sprintDescription"]')
        .clear()
      .type('This is a description 2')
    cy.get('[id="sprintStartDate"]')
        .clear()
      .type('2023-05-01')
    cy.get('[id="sprintEndDate"]')
        .clear()
      .type('2023-05-15')
    cy.get('[data-cy="edit-sprint-save"]').click()
    cy.contains('Sprint dates must not overlap with other sprints. Dates are overlapping with 20/Apr/2023 - 05/May/2023').should('be.visible')
  })
})