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
      cy.get('[data-cy="addSprintStartDate"]')
              .clear()
            .type('2023-04-10')
      cy.get('[data-cy="addSprintEndDate"]')
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
     cy.get('[data-cy="addSprintStartDate"]')
             .clear()
           .type('2023-05-04')
     cy.get('[data-cy="addSprintEndDate"]')
             .clear()
           .type('2023-05-20')
     cy.get('[data-cy="add-sprint-save"]').click()
     cy.contains('Sprint 2').should('be.visible')
   })

  it('edits a sprint with new values', () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('.edit-button').click()
        cy.get('[data-cy="editSprintName"]').clear()
                .type('This is a sprint')
        cy.get('[data-cy="editSprintDescription"]').clear()
                .type('This is a description')
        cy.get('[data-cy="editSprintStartDate"]').clear()
                .type('2023-04-10')
        cy.get('[data-cy="editSprintEndDate"]').clear()
                .type('2023-04-20')
        cy.get('[data-cy="edit-sprint-save"]').click()
    })
    cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('.sprint-title').should('have.text', "This is a sprint")
        cy.get('.sprint-description').should('have.text', "This is a description")
        cy.get('.sprint-date').should('have.text', "10/Apr/2023 - 20/Apr/2023")
    })
  })

  it('edits a sprint with a changed name', () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('.edit-button').click()
        cy.get('[data-cy="editSprintName"]').clear()
                .type('This is a test sprint')
        cy.get('[data-cy="edit-sprint-save"]').click()
    })
    cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('.sprint-title').should('have.text', "This is a test sprint")
    })
  })

  it('edits a sprint with a changed description', () => {
  loadCookies()
    cy.visit('/project/0')
     cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('.edit-button').click()
        cy.get('[data-cy="editSprintDescription"]').clear()
                .type('This is a test description')
        cy.get('[data-cy="edit-sprint-save"]').click()
    })
    cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('.sprint-description').should('have.text', "This is a test description")
    })
  })

  it('edits a sprint with changed dates', () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('.edit-button').click()
        cy.get('[data-cy="editSprintStartDate"]').clear()
                .type('2023-04-20')
        cy.get('[data-cy="editSprintEndDate"]').clear()
                .type('2023-04-30')
        cy.get('[data-cy="edit-sprint-save"]').click()
    })
    cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('.sprint-date').should('have.text', "20/Apr/2023 - 30/Apr/2023")
    })
  })
})

describe("I can't edit an invalid sprint", () => {

  it("doesn't edit a sprint without a name", () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('.edit-button').click()
        cy.get('[data-cy="editSprintName"]').clear()
                .type('    ')
        cy.get('[data-cy="edit-sprint-save"]').click()
    })
    cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('#nameFeedback').should('have.text', "Name cannot be blank")
    })
  })

  it("doesn't edit a sprint with an invalid name", () => {
  loadCookies()
    cy.visit('/project/0')
    cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('.edit-button').click()
        cy.get('[data-cy="editSprintName"]').clear()
                .type('<New Sprint>')
        cy.get('[data-cy="edit-sprint-save"]').click()
    })
    cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
        cy.get('#nameFeedback').should('have.text', "Name can only have letters, numbers, punctuations except commas, and spaces.")
    })
  })

  it('edits a sprint with start date being more than end date', () => {
    loadCookies()
      cy.visit('/project/0')
      cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
          cy.get('.edit-button').click()
          cy.get('[data-cy="editSprintStartDate"]').clear()
                  .type('2023-04-10')
          cy.get('[data-cy="editSprintEndDate"]').clear()
                  .type('2023-04-05')
          cy.get('[data-cy="edit-sprint-save"]').click()
      })
      cy.get('[data-cy="sprintEdit-Sprint 1"]').within(() => {
          cy.get('#endDateFeedback').should('have.text', "Start date must always be before end date")
      })
    })

  it("doesn't edit a sprint overlapping another sprint", () => {
    loadCookies()
      cy.visit('/project/0')
      cy.get('[data-cy="sprintEdit-Sprint 2"]').within(() => {
          cy.get('.edit-button').click()
          cy.get('[data-cy="editSprintName"]').clear()
                  .type('This is a sprint 2')
          cy.get('[data-cy="editSprintDescription"]').clear()
                  .type('This is a description 2')
          cy.get('[data-cy="editSprintStartDate"]').clear()
                   .type('2023-04-29')
          cy.get('[data-cy="editSprintEndDate"]').clear()
                  .type('2023-05-15')
          cy.get('[data-cy="edit-sprint-save"]').click()
      })
      cy.get('[data-cy="sprintEdit-Sprint 2"]').within(() => {
        cy.get('#startDateFeedback').should('have.text', "Sprint dates must not overlap with other sprints. Dates are overlapping with 20/Apr/2023 - 30/Apr/2023")
      })
    })
})