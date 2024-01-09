Instituto Superior Técnico, Universidade de Lisboa

**Network and Computer Security**

# Preamble

This document explains the project scenarios.  
Please read the project overview first.

The scenarios describe a richer application context than the one that will be actually need to be implemented.  
The security aspects should be the focus of your work.
User interface design, although critical in a real application, is secondary in this project.

Each project team can change the document format for each scenario, but the fields shown must still be included in some way.

# Project Scenarios

_Congratulations! You have been hired by one of the following companies._

Each company has its business context and, more importantly, a document format that is pivotal for their business.  
Later in the project, a security challenge will be revealed and your team will have to respond to it with a suitable solution.

----

## 3. Insurance & Banking: BlingBank

BlingBank is a contemporary digital financial platform, built on the principles of accessibility and convenience.
BlingBank provides an online banking platform, accessible via a web application.  
The main functionalities are: account management, expense monitoring, and payments.
The account management allows an efficient oversight of account balance.
Expense monitoring shows the movements corresponding to expenses, in categories.
Finally, payments allow a simple way to make bill payments.

The core data handled by the application is exemplified next:

```json
{
  "account": {
    "accountHolder": ["Alice"],
    "balance": 872.22,
    "currency": "EUR",
    "movements": [
      {
        "date": "09/11/2023",
        "value": 1000.00,
        "description": "Salary"
      },
      {
        "date": "15/11/2023",
        "value": -77.78,
        "description": "Electricity bill"
      },
      {
        "date": "22/11/2023",
        "value": -50.00,
        "description": "ATM Withdrawal"
      }
    ]
  }
}
```

### Protection Needs

The protected document must ensure the _authenticity_ and _confidentiality_ of the account data.  
You can assume that the user and the service share a secret key.

### Security Challenge

Introduce a new document format specifically for _payment orders_.
This format should be designed to guarantee confidentiality, authenticity, and non-repudiation of each transaction.  
Implement robust freshness measures to prevent duplicate executions of the order.
A duplicate order should never be accepted.  
Also, for accounts with _multiple owners_, e.g. Alice and Bob, require authorization and non-repudiation from all owners before the payment order is executed.  
Given the new requirements above, especially non-repudiation, each user will likely need some new keys, and a dynamic key distribution will have to be devised, starting with the existing keys.

To support these new requirements, the cryptographic library (including the CLI) and the infrastructure should be extended as needed.
----

[SIRS Faculty](mailto:meic-sirs@disciplinas.tecnico.ulisboa.pt)
