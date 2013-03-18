#KitchenSink#

##So you want to:##


###add a feature?###
 
Features are declared as Activities, and there are three steps to adding a new feature.

1.  Declare the new Activity in the manifest, and give it a title in strings.xml.  There's really no way around this, but it should be trivial.  Feel free to copy and paste the other Activity declarations, just change the class name and the title value.

2.  Have your class extend FeatureActivity, which is an abstract class that takes care of everything.  There is one method to @Override, which returns a list of all Use Case Fragments (see below) associated with that Feature. 

3.  Open up Loader.Java, and create a new Feature Object.  Give the new feature a Display Name, a blurb (light description), as well as a reference to the new Activity.class   

###add a use case to a feature?###

1.  Declare the XML layout for the fragment.  
2.  Create a new Class, and have it extend a UseCaseFragment.  This abstract class requires a handful of overrides:

		* getViewID() -> have this method return the R.layout.* you created in step 1.
		
		* bindViews(View v) ->  This is called when the Fragment's View is created, so use this method to establish references to the View objects in your xml.  Note that `View v` is the parent view, so use something like:  myTextView = (TextView) v.findViewById(R.id.myTextView);  Also, set onClickListeners and other Observers in this method.
		
		* populateViews() -> This method is called onResume(), so use it to push data to the View objects set in bindViews.  While it might not be necessary for KitchenSink, this will allow the UI to be updated everytime the user returns to the Application.  for example. myTextView.setText("Hello!");
		
		* getTitle() -> Use this method to return a String Title of this specific use case.  Keep it short, at one or two words.  This is used by the ViewPagerIndicator to allow for navigation.
		
3.  Go back to the Activity for the Feature, and add this new fragment to the list returned in the @Override method there.



##Organization and Design

Every feature is given it's own package-- this package can contain the Activity as well as the Fragments and any other necessities.

The Launch Activity will pull a list of all Features from the Loader.java class, and display these in a listview.  When tapping on a feature, the appropriate activity will be started.  Each FeatureActivity contains a ViewPager, which is populated by the List of Fragments provided by the @Override method.

Once the Fragments are displayed in the view pager, the above abstract methods are called which will allow us to build up custom views and behavior for each fragment.