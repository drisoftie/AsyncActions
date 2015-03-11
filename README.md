# AsyncActions
Library to help with asynchronous work in an AsyncTask like way. 
Based on [java.lang.reflect.Proxy][1]. it implements any Java Interface automatically and dispatches any invocation of them in a three step process, of which the first is executed directly on the invocation call, the second in a separate thread and the third is guranteed to be executed inside the Android UI Thread.

    // is ALWAYS executed inside the same thread it was invoked in
    Object onActionPrepare(String methodName, Object[] methodArgs, 
                           Tag1T tag1, Tag2T tag2, Object[] additionalTags);

    // is ALWAYS executed inside a separate thread
    ResultT onActionDoWork(String methodName, Object[] methodArgs,
                           Tag1T tag1, Tag2T tag2, Object[] additionalTags);

    // is ALWAYS executed inside the UI thread
    void onActionAfterWork(String methodName, Object[] methodArgs,
                           ResultT workResult, Tag1T tag1, Tag2T tag2, Object[] additionalTags);

### Usage

You can use it in two ways.

Either by using the `ActionBuilder`:

    new ActionBuilder<>().
          // register a Button
          with(myButton).
          // implement the OnClickListener with the given method "setOnClickListener"
          reg(OnClickListener.class, RegActionMethod.SET_ONCLICK).
          // in addition implement a generic interface
          reg(IGenericAction.class, null).
          // filter out everything except those two methods
          invokeOnly(ActionMethod.ON_CLICK, ActionMethod.INVOKE_ACTION).
          // pack everything above inside this action
          pack(new AndroidAction<View, Result, Void, Void>(null, null) {

          @Override
          public Object onActionPrepare(String methodName, Object[] methodArgs, Void tag1, Void tag2,
                                        Object[] additionalTags) {
              if (ActionMethod.ON_CLICK.matches(methodName)) {
                  // do something when button is clicked, e.g. display progress bar
              } else if (ActionMethod.INVOKE_ACTION.matches(methodName)) {
                  // do something when generic action is invoked, e.g. show dialog
              }
              return null;
          }

          @Override
          public Result onActionDoWork(String methodName, Object[] methodArgs, Void tag1, Void tag2,
                                       Object[] additionalTags) {
              // calculate result and return it
              return result;
          }

          @Override
          public void onActionAfterWork(String methodName, Object[] methodArgs, Result workResult, Void tag1,
                                        Void tag2, Object[] additionalTags) {
              // show user the results
          }
    });

Or use the `AndroidAction` class and its many constructors itself:

    new AndroidAction<View, Result, Void, Void>(button, 
                                                // implement two interfaces
                                                new Class[]{OnClickListener.class, IResultHandler.class},
                                                // only one interface needs to be bound to a view
                                                RegActionMethod.SET_ONCLICK.method()) {

          @Override
          public Object onActionPrepare(String methodName, Object[] methodArgs, Void tag1, Void tag2, 
                                        Object[]  additionalTags) {
              if (ActionMethod.ON_CLICK.matches(methodName)) {
                  // show a dialog when button is clicked, then cancel background threading, because it's not needed
                  skipWorkThreadOnce();
                  return null;
              } else if () {
                  // do something with the dialog when it returns a result
                  return null;
              }
          }    

          @Override
          public NdefEntity onActionDoWork(String methodName, Object[] methodArgs, Void tag1, Void tag2,
                                           Object[] additionalTags) {
              if (ActionMethod.ON_RESULT_READY.matches(methodName)) {
                  // or do something in background with the result
              }
              return null;
          }

          @Override
          public void onActionAfterWork(String methodName, Object[] methodArgs, Result workResult, Void tag1,
                                        Void tag2, Object[] additionalTags) {
              // display something to the user
          }
    };


  [1]: http://docs.oracle.com/javase/7/docs/api/java/lang/reflect/Proxy.html "JavaDoc"
