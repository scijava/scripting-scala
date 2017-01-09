// @String(label="Please enter your name",description="Name field") name
// @OUTPUT String greeting

// A Scala script with parameters
// It is the duty of the scripting framework to harvest
// the 'name' parameter from the user, and then display
// the 'greeting' output parameter, based on it's type.

val greeting = s"Hello, ${name}!"
