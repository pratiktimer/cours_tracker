# Clean Architecture + TDD + MVVM Migration Guide

## Architecture Overview

```

                  PRESENTATION LAYER                  
       
    Composables   ViewModels    UI States  
       

                       

                   DOMAIN LAYER                       
       
     Use Cases       Models       Repository  
                    (Entities)    Interfaces  
       

                       

                    DATA LAYER                        
       
   Repository       Data            Room      
   Implementation    Sources        Database   
       

```

## Migration Steps

### Step 1: Update Dependencies
### Step 2: Create Domain Layer
### Step 3: Create Data Layer
### Step 4: Create Presentation Layer
### Step 5: Setup Hilt DI
### Step 6: Write Tests
### Step 7: Migrate UI

## File Structure

```
app/src/
 main/
    java/com/prateektimer/course_tracker/
        data/
           local/
              dao/
              entity/
              AppDatabase.kt
           repository/
           mapper/
        domain/
           model/
           repository/
           usecase/
        presentation/
           courselist/
              CourseListViewModel.kt
              CourseListScreen.kt
              CourseListState.kt
           videolist/
           common/
        di/
        MainActivity.kt
 test/
     java/com/prateektimer/course_tracker/
         domain/usecase/
         data/repository/
         presentation/
```
