## Description of JSON-format used for indexing goals and tasks 

Each file from the folder "Regleringsbrev" is indexed to elastic search using he format showed in the example below. The following keys and values are stored: 

#### authority 
String with the name of the authority 

#### year 
Integer with the year of the document 

#### filename
String with the full filename of the document 

#### report 
The årsredovisning from the same authority, the following year. Meaning that this is the document where results of working with the goals and tasks are found. 

#### goals 
A list of objects containing all extracted goals from this file. Each goals contains a header and a content. The headers can be the same for several goals.  

#### tasks 
A list of objects containing all extracted tasks from this file. Each task contains a header and a content. The headers can be the same for several goals.  


```javascript
{
  "authority": "Myndighet Affärsverket svenska kraftnät",
  "year": 2006,
  "filename": "2006_Affärsverket svenska kraftnät_2021004284.html",
  "report": "2007_Affärsverket svenska kraftnät_2021004284.html",
  "goals": [
    { "goal_header": "header goal 1",
      "goal_content": "This is example goal 1",
    },
    { "goal_header": "header goal 2",
      "goal_content": "this is example goal 2",
    }
    { "header": "header goal 3",
      "goal_content": "this is example goal 3",
    }
  ],
  "tasks": [
    { "goal_header": "header task 1",
      "goal_content": "This is example task 1",
    },
    { "goal_header": "header task 2",
      "goal_content": "This is example task 1"
    }
    { "goal_header": "header task 3",
      "goal_content": "This is example task 1"
    }
  ]
}
```

