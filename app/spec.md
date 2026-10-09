# Activity Tracker Specification 

## Executive Summary & Context
* Activity Tracker is a native Android application that enables users to easily capture information about their meals and understand the nutrients they're consuming.

## Goal
* Create the layout xml, style xml, manifest and java code for this application.

## Scope
* **Functional requirement 1.1:** As the user in the configuration view, I want to define the number of meals in a day and a custom name for each meal. 
* **Functional requirement 2.1:** As the user in the planning view I want to select one of day's meals to plan.
* **Functional requirement 2.2:** As the user in the planning view, when planning a meal I want to select any number of food & weight pairs but typically around 8. Until the meal is committed I want to be able to modify food, modify weight, add food & weight pairs and remove food & weight pairs. Until the meal is committed I want the ability to enter and modify the meal start time, unsulin dosage amount and insulin injection time.
* **Functional requirement 2.3:** As the user in the planning view, I want to commit the day's meals. This should prompt for a date that will be associated with the meals, defaulted to today's date. Committing the day's meals prevents future changes to the meals.
* **Functional requirement 2.4:** As the user in the planning view, I want to see the food level, meal level and total aggregation of primary macronutrients: calories, digestable carbohydrates (total carbohydrates minus digestable fiber), saturated fat, fat, protein. At the food level, meal level and total aggregation the user should see secondary macronutrients but less prominently, possibly saving screen real estate: fiber, cholesterol, carbohydrates, omega 3 fats, omega 6 fats.
* **Functional requirement 2.5:** As the user in the planning view, I want to select from a configurable list of foods when updating food rows.
* **Functional requirement 2.6:** As the user in the planning view, when planning a meal I want the option to populate its food & weight pairs and insulin dosage amount from the most recently committed meal with the same name.
* **Functional requirement 2.7:** As the user in the planning view, when planning a meal I want the option to populate its food & weight pairs and insulin dosage amount from a template.
* **Functional requirement 2.8:** As the user in the planning view, I should not be allowed to commit the day's meals if any of them have unrecognized food or undefined weight.
* **Functional requirement 3.1:** As the user in the template view, I want to be able to create, edit and delete meal templates. The user should be able to assign the template a unique name. A meal template is comprised of food & weight pairs and and insulin dosage amount, exactly like a meal. While creating the template I want to see the primary and secondary nutrients for each food/weight pair. I also want to see the template level aggregation of primary and secondary nutrients.
* **Functional requirement 3.2:** As the user in the template view, I should not be allowed to submit a meal template that has unrecognized food or undefined weight.
* **Functional requirement 4.1:** As the user in the history view, I want to see past meals that I have committed.
* **Functional requirement 4.2:** As the user in the history view, I want to be able to adjust a day that I have committed. Doing this clears all food/weight pairs from the planning view, makes the selected day's meals uncommitted and opens the previously committed food/weight pairs for the day in the planning view.
* **Functional requirement 5.1:** As the user in the food view, I want to view a list of unarchived foods.
* **Functional requirement 5.2:** As the user in the food view, I want to archive a food. This should bring up a confirmation dialogue allowing the user to cancel or confirm the archival. Archiving a food hides it from the food list and prevents it from appearing in the planning and template view's food autocomplete selector.
* **Functional requirement 5.3:** As the user in the food view, I want to the option of viewing archived foods.
* **Functional requirement 5.4:** As the user in the food view, I want to edit an existing food. 
* **Functional requirement 5.5:** As the user in the food view, editing or creating a new food should allow the user to specify values for weight, calories, digestable carbohydrates (total carbohydrates minus digestable fiber), saturated fat, fat, protein, fiber, cholesterol, carbohydrates, omega 3 fats, omega 6 fats.
* **Functional requirement 5.6:** As the user in the food view, editing or creating a new food should allow the user to specify values for the food's name, weight, calories, digestable carbohydrates (total carbohydrates minus digestable fiber), saturated fat, fat, protein, fiber, cholesterol, carbohydrates, omega 3 fats, omega 6 fats.
* **Functional requirement 5.7:** As the user in the food view, when attempting to submit either a food edit or new food, it should be required to have a unique name.
* **Functional requirement 5.7:** As the user in the food view, when attempting to submit either a food edit or new food, I should not be allowed to have undefined values for any of the food's nutritients.

## Technical Constraints
* Native android application written in Java. The root java directory should be com.activitytracker.

## User Flows & Interface
* **User Flow:** After the user launches the application they'll be taken to the meal planning view. In this view they'll have the ability to see all uncommitted meals starting with the most recent one but with the ability to navigate to earlier meals. From here they can either modify an existing, uncommitted meal or create a new one. The user will add, update and remove food rows from the meal. Adjusting meal weight will not trigger a change in nutrient roll up until the field loses focus, either by interacting outside the field or pressing enter on the keyboard. Food is adjusted by typing in the food field, which brings up a list of autocompleted foods for the user to choose. At any time, the user can enter an insulin amount, insulin time and meal time. After these details are final they'll commit the meal. On successful commit, the meal will dissapear from the meal planning view.
* **Component Layout:** The app should avoid overlap with system bars using classes such as androidx.activity.EdgeToEdge and androidx.core.view.WindowInsetsCompat. Both the day level and meal level aggregation should always be visible when adding, adjusting or removing food & weight pairs so that the user can see the nutrient impact as food and weight changes. When the virtual keyboard pops up the screen should resize appropriately to avoid hiding either the focus field or autocomplete menu.
* **Style:** Apply Material Design 3 style with the Clinical Vitality color palette. Ensure that the color pallet works for both light and dark mode. Ensure that text in the autocomplete box can be seen clearly.
