### Promp: Pre-Setup the Engineering Disciplines

```jsx
read the Claude.md file, then expland the guildline and coding rules to me.
Let`s make sure we have the same understanding about the engineering disciplines for the project.
(Do Not Implement the codes right now)
```

### Promp: Auto Generate the Flow Diagram and ER Models for analysis

```jsx
analysis the overall service flow of the API endpoint **/goods/getGoodsDetailV5.service**, based on the conditions below:

[Must To Have]
- API endpoint: **/goods/getGoodsDetailV5.service**
- Controller: **GoodsNativeController**
- Dig all the dependency services involved in the process and their interactions into method level details automatically.
- Identify the sequence of interactions among components
- Highlight the cross system/app interactions (e.g., Kafka, Redis, Restful API invocation... etc.)
- Arrange all the data models used in the process with their relationships into ER diagram
- Arrange all the tables and the relationship in SQL commands
- Arrange all active diagam and sequence diagram of the process.
- Arrange all test scenarios
- All diagrams are generated with the **PlantUML** diagram **puml** format.
- All analysis documents are generated with the **Markdown** format.
- Generate the analysis report into middle/doc/${API_PATH} folder
- **Do NOT overwrite existing files directly**, append new information to existing files or create new files with different names [!Important]
- **Do NOT implement code right now** - this is analysis phase only [!Important]
```

### Promp: Auto Generate the Fundamental APIs, Flows and Objects Design Documents [Refactor]

```jsx
great, base on the disciplines and the analysis documents **above**,
I`d like to refactor this function from **middle-web** into **goods** module,
provide the design documents with the conditions below:

- jdk8 + spring boot2
- os: window, cmd: power shell (if needed)
- full fundamental apis
- full biz flows, cross function interaction flows and utils
- all class and object needed
- all test cases, test senarios and test data with gherkin format
- swagger api docs
- use the library in pom only
- **Do NOT overwrite existing files directly**, append new information to existing files or create new files with different names [!Important]
```

### Promp: Auto Implement the Fundemantal APIs, Flows and Objects by Design Documents [Refactor]

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

### Promp: Auto Generate the Fundamental APIs, Flows and Objects Design Documents [New Requirements]

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
- Generate the analysis report into middle/doc/${API_PATH} folder
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
