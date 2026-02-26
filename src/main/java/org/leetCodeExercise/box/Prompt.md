### Promp: Pre-Setup the Engineering Disciplines

```jsx
read the Claude.md file, then expland the guildline and coding rules to me.
Let`s make sure we have the same understanding about the engineering disciplines for the project.
(Do Not Implement the codes right now)
```

### Promp: Auto Generate the Flow Diagram and ER Models for analysis [Analysis]

```jsx
analysis the overall service flow of the API endpoint **/getAvgWeigBuyPrice.service**, based on the conditions below:

[Must To Have]
- API endpoint: **/getAvgWeigBuyPrice.service**
- Controller: **GoodsController**
- Dig all the dependency services involved in the process and their interactions into method level details automatically.
- Identify the sequence of interactions among components
- Highlight the cross system/app interactions (e.g., Kafka, Redis, Restful API invocation... etc.)
- Arrange all the data models used in the process with their relationships into ER diagram
- Arrange all the tables and the relationship in SQL commands
- Arrange all active diagam and sequence diagram of the process.
- Arrange all test scenarios
- All diagrams are generated with the **PlantUML** diagram **puml** format.
- All analysis documents are generated with the **Markdown** format.
- Generate the analysis report into folder middle/doc/goods/${API_PATH}/
- **Do NOT overwrite existing files directly**, append new information to existing files or create new files with different names [!Important]
- **Do NOT implement code right now** - this is analysis phase only [!Important]
```

### Promp: Auto Generate the Fundamental APIs, Flows and Objects Design Documents [Refactor - Design]

```jsx
great, base on the disciplines and the analysis documents **above**,
I`d like to refactor this function in **goods** module,
provide the design documents with the conditions below:

- jdk8 + spring boot2
- os: window, cmd: power shell (if needed)
- all Model/DTO/RequestDTO/ResponseDTO
- all test cases, test senarios and test data with gherkin format
- swagger api docs
- use the library in pom only
- **Do NOT overwrite existing files directly**, append new information to existing files or create new files with different names [!Important]
```

### Promp: Auto Implement the Fundamental APIs, Flows and Objects by Design Documents [Refactor - Complete Shadow API Test]

```jsx
great, i just completed the design review, and everything looks good.
let`s implement the test cases within the Shadow Testing pattern and the extra specs below:

[Must To Have]
- Based on the Shadow Testing Guides (${ProjectRoot}/doc/testing/ShadowTestingGuide.md) 
- Target Module: **goods-api**
- Action: **Testing**
- Methodology: **Shadow Testing, TDD**
- Reference Samples:
    - BaseShadowTest
    - DualShadowTestController_testGetTest (Must to follow the structure and format)
    - testGetTest.yml (Must to follow the structure and format)
    - testGetTest.json (Must to follow the structure and format)
- Implementation Steps:
    - Focus on the Shadow Testing implementation only
    - Arrange all the scenarios into src/test/resources/scenario/  (Must to follow the structure and format)
    - Arrange all the specifications into src/test/resources/specification/  (Must to follow the structure and format)
	- Implement all Shadow Test scenarios with test data in Gherkin format for A/B Testing. (exclude the Performance Test)
	- Arrange the native query sql to collect the test data for each test case in database.
	- Provide the docuemtation files in the scope above only. (e.g., Don`t provide any md files not in the scope above)
 	- **Recheck the generated files for each file. If the file is not valid, please re-generate it until it is correct.**
- **Do NOT overwrite existing files directly**, append new information to existing files or create new files with different names [!Important]
```

### Promp: Auto Implement the Fundamental APIs, Flows and Objects by Design Documents [Refactor]

```jsx
cool, i just completed the design review, and everything looks good.
let`s implement the codes based on the design documents above with the extra specs below:

[Must To Have]
- Module: **goods**
- Action: **Refactor**
- Methodology: **TDD**
- Implementation Steps: Model/DTO -> Test -> Service/Dao -> Controller (waiting for reviewing in each step)
- **Do NOT overwrite existing files directly**, append new information to existing files or create new files with different names [!Important]
- Specs (**Gherkin** Format Spec):
	- Include all the Specs above, then design the related Test Cases
	- New Spec:
		Given **xxx,**
		When **xxx,**
		And **xxx,**
		Then **xxx**
	- Flow Chat / ER Model Diagram )
- Test Cases (**Gherkin** Format Spec):
	- Include all the Specs above, then design the related Test Cases
	- New Test Case: **xxx,**
		Given **xxx,**
		When **xxx,**
		And **xxx,**
		Then **xxx**
	- Flow Chat / ER Model Diagram )
```

### Promp: Auto Generate the Fundamental APIs, Flows and Objects Design Documents [New Requirements - Frontend]

```jsx
base on the attached folders, specs and files,
let`s analysis the requirements and provide the system design documents in project path 'doc/${Requirement}/'.
all analysis and design documents should include the conditions below:

[Must To Have]
- Identify the User Behavior in active diagram
- Arrange all the UI components used in the process with their relationships into class diagram
	(We will implement the function into React functional components with Dependency Inject design concept)
- Arrange all the data models used in the process with their relationships into class diagram
- Arrange all test scenarios
- All diagrams are generated with the **PlantUML** diagram **puml** format.
- All analysis documents are generated with the **Markdown** format.
- Generate the analysis report into ${ProjectRoot}/doc/${Requirement} folder
- **Do NOT overwrite existing files directly**, append new information to existing files or create new files with different names [!Important]
- **Do NOT implement code right now** - this is analysis phase only [!Important]
```

### Promp: Auto Generate the Fundamental APIs, Flows and Objects Design Documents [New Requirements - Backend]

```jsx
base on the attached specs and files, let`s provide the new design document with the conditions below:

[Must To Have]
- API endpoint in Google AIP Style
- Controller, Service, Dao, DTO class design
- Dig all the dependency services involved in the process and their interactions into method level details automatically.
- Identify the sequence of interactions among components
- Highlight the cross system/app interactions (e.g., Kafka, Redis, Restful API invocation... etc.)
- Arrange all the data models used in the process with their relationships into ER diagram
- Arrange all the tables and the relationship in SQL commands
- Arrange all active diagam and sequence diagram of the process.
- Arrange all test scenarios
- All diagrams are generated with the **PlantUML** diagram **puml** format.
- All analysis documents are generated with the **Markdown** format.
- Generate the analysis report into ${ProjectRoot}/doc/${API_PATH} folder
- **Do NOT overwrite existing files directly**, append new information to existing files or create new files with different names [!Important]
- **Do NOT implement code right now** - this is analysis phase only [!Important]
```

### Promp: Auto Implement the Fundemantal APIs, Flows and Objects by Design Documents [New Requirements]

```jsx
cool, i just completed the design review, and everything looks good.
let`s implement the codes based on the design documents above with the extra specs below:

[Must To Have]
- Module: **goods**
- Action: **Implement**
- Methodology: **TDD**
- Implementation Steps: Model/DTO -> Test -> Service/Dao -> Controller (waiting for reviewing in each step)
- **Do NOT overwrite existing files directly**, append new information to existing files or create new files with different names [!Important]
- Specs (**Gherkin** Format Spec):
	- Include all the Specs above, then design the related Test Cases
	- New Spec:
		Given **xxx,**
		When **xxx,**
		And **xxx,**
		Then **xxx**
	- Flow Chat / ER Model Diagram )
- Test Cases (**Gherkin** Format Spec):
	- Include all the Specs above, then design the related Test Cases
	- New Test Case: **xxx,**
		Given **xxx,**
		When **xxx,**
		And **xxx,**
		Then **xxx**
	- Flow Chat / ER Model Diagram )
```

### Promp: Refine the Spec with Gherkin Specs

```jsx
Based on the Engineering Disciplines and previous discussions,
**retain all existing information** and **append the following Specs and Test Cases** into the design documents,
then **provide the updated design document version**:

- Specs (**Gherkin** Format Spec), **New / Modify**:
	- Given **xxx**,
		When **xxx**,
		And **xxx**,
		Then **xxx**
	- Flow Chart / ER Model Diagram

- Test Cases (**Gherkin** Format Spec), **New / Modify**:
	- Test Case: **xxx**,
		Given **xxx**,
		When **xxx**,
		And **xxx**,
		Then **xxx**
	- Flow Chart / ER Model Diagram

```

### Promp: Process Broken Due to Token Limits or Other Issues

```jsx
keep the contexts, then continue and complete the process
```
