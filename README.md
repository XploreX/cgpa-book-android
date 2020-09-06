# cgpa-book-android

## Brief overview of the files

All the kotlin files and layout resource files are organised by categories based on the type they are used.
For looking at the structured view, use the Project view

![](https://github.com/manorit2001/cgpa-book-android/blob/master/static/img/cgpabook_project_view.png)

### The Kotlin files
![](https://github.com/manorit2001/cgpa-book-android/blob/master/static/img/cgpabook_dir_structure_1.png)

- Activity
  - **MainActivity.kt** : Handles the Sign In Activity(only Google SignIn implemented)
  - **NavigationActivity.kt** : This is the user main activity after sign in. It is linked with the fragments in `ui` package
  - **SearchActivity.kt** : Start this activity for a result with arraylist in intent with key *"list"* and it will return the selected element with key *"selected"*
- Adapter
  - **RecyclerAdapter.kt** : Used for SearchActivity recycler view
- Classes
  - **CollegeChooseModel.kt** : Used for CollegeChooseFragment.kt
  - **SubjectsData.kt** : Used for EnterMarksFragment.kt
- UI
  - **SharedViewModel.kt** : 
    - A dynamic view model which will stays in NavigationActivity.kt context
    - It has a template invokation of new MutableLiveData variables
      - `viewModel.getElement<T>("Tag")` : returns the MutableLiveData<T> Element if exist or creates a new one if it doesn't and returns the newly created on
      - `viewModel.getVal<T>("Tag")` : returns the value of the element
      - `viewModel.setVal<T>("Tag",val)` : sets the value of the element
        (getVal and setVal both handles automatic creation of variable if doesn't exist)
  - Profile
    - **ProfileFragment.kt** : This is the main profile fragment visible to use after logging in(work pending) -> returns the value of the element
  - UpdateCGPA
    - **CollegeChooseFragment.kt** : Calls SearchActivity for each of the fields and stores the data in view model so that it remains for the duration of the activity
    - **EnterMarksFragment.kt** : This comes after above fragment and handles calling the subjects api and retrieving the subjects and the grades and updates CGPA
    - **NewCreditsFragment.kt**(Backend pending) : Will handle the new credits schema being entered if it does not exist
 - Utils
    - **Extensions.kt** : Has many extension functions which can be reused for various stuffs
    - **HelperStrings.kt** : Define the static strings which will be used in various files
    - **JsonArrayRequestCached** : Implements a cached variant of JsonArrayRequest
    - **JsonObjectRequestCached** : Implements a cached variant of JsonObjectRequest
    - **MySingleton** : Creates a Volley object in the application context to be reused


### The layout files
![](https://github.com/manorit2001/cgpa-book-android/blob/master/static/img/cgpabook_dir_structure_2.png)

Create new files in this categorised order only.
