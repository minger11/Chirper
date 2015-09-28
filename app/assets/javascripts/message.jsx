//html element to render from scala template
var mountNode = document.getElementById('renderMessages')


//string from scala template
//indicates whether user is logged in
var loggedIn = document.getElementById('loggedIn').value

//TIMESINCE
//takes millisecond interval and converts it to minutes, hours, days, weeks, months and years
var timeSince = function(date) {
        var seconds = Math.floor(date / 1000);
        var interval = Math.floor(seconds / 31536000);
        if (interval > 1) {
            return interval + " years";
        }
        interval = Math.floor(seconds / 2592000);
        if (interval > 1) {
            return interval + " months";
        }
        interval = Math.floor(seconds / 86400);
        if (interval > 1) {
            return interval + " days";
        }
        interval = Math.floor(seconds / 3600);
        if (interval > 1) {
            return interval + " hours";
        }
        interval = Math.floor(seconds / 60);
        return interval + " minutes";
    };

//MESSAGE
var Message = React.createClass({
  //initializes state if current user is logged in
  getInitialState: function() {
       if(loggedIn == "yes") {
          return {
            searchString: ""
          }
       }
  },
  //prepares the state for a user search based on the user of the current message
  setSearchString: function(e) {
        var s = this.state
        s["searchString"] = "@"+this.props.message.email
        this.setState(s)
      },
  //performs a message search on the current state
  //opens a websocket based on the message search
  handleSearchSubmit: function(e) {
          e.preventDefault()
          window.getMessage(this.state)
          window.getSocket(this.state)
  },
  render: function() {
    //the message object
    var obj = this.props.message
    //time since message was posted
    var time = timeSince(obj.age)
    //returns a message panel with user, time, message and tags
    //provides a user-based search hyperlink on the focused user
    return <div className="panel panel-default">
               <div className="panel-heading">
                      <div className="user"><a href="#" onFocus={this.setSearchString} onClick={this.handleSearchSubmit}><strong>{obj.email}</strong></a><div className="pull-right"><sup>{time} ago</sup></div></div>
               </div>
               <div className="panel-body">
                      <div className="message">{obj.message}</div>
               </div>
               <div className="panel-footer">
                                     <TagList tags={obj.tags}/>
                              </div>
    </div>
  }
});

//MESSAGE LIST
//takes a jsonarray of messages and sends each message to the message function
var MessageList = React.createClass({
  render: function() {
    return <ul>{this.props.messageList.map(function(item) {
      return <Message message={item} />
    })}</ul>;
  }
});

//TAG LIST
//takes a json array of tags and sends each tag to the tag function
var TagList = React.createClass({
  render: function() {
    return <div>{this.props.tags.map(function(item) {
        return <Tag tag={item}/>
    })}</div>;
}});

//TAG
var Tag = React.createClass({
//initializes the state
getInitialState: function() {
        return {
          searchString: ""
        }
      },
//sets the state to be that of the current tag
setSearchString: function(e) {
      var s = this.state
      s["searchString"] = this.props.tag.tag
      this.setState(s)
    },
//performs a message search on the current state
//opens a websocket based on the message search
handleSearchSubmit: function(e) {
        e.preventDefault()
        window.getMessage(this.state)
        window.getSocket(this.state)
    },
//provides a hyperlink to perform a tag-based search on the focused tag
render: function() {
        return <a href="#" onClick={this.handleSearchSubmit} onFocus={this.setSearchString}><sup>#{this.props.tag.tag} </sup></a>
  }
});

//USEROPTIONS
var UserOptions = React.createClass({
//initializes current state
  getInitialState: function() {
     if(loggedIn == "yes") {
        return {
          //prepares the state for a user-based search
          searchString: "@"+document.getElementById('userEmail').value
        }} else { return {
                  //prepares the state for a default search
                  searchString: ""
                 }
               }
  },
  //performs a message search on the current state
  //opens a websocket based on the message search
  handleSearchSubmit: function(e) {
        e.preventDefault()
        window.getMessage(this.state)
        window.getSocket(this.state)
  },
  render: function() {
  //Provides a user-labelled dropdown with various options
  if(loggedIn == "yes") {
    var userEmail = document.getElementById('userEmail').value
    return <li className="dropdown">
                <a href="" className="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                    {userEmail}
                    <span className="caret"></span></a>
                <ul className="dropdown-menu">
                    <li><a href="#" onClick={this.handleSearchSubmit}>My Chirps</a> </li>
                    <li><a href="sessions">Active Sessions</a></li>
                    <li><a href="logout">Logout</a></li>
                </ul>
            </li>
    }else
    //advises not logged-in dropdown and provides a register button
    {return <li className="dropdown">
                     <a href="" className="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                         Not logged in
                         <span className="caret"></span></a>
                         <ul className="dropdown-menu">
                             <li><a href="register">Register</a></li>
                         </ul>
                 </li>
         }
  }
});

//SEARCHBAR
var SearchBar = React.createClass({
  //initializes the current state as a default string
  getInitialState: function() {
      return {
        searchString: ""
      }
    },
  //if logged-in, performs a (default) search based on the current state
  //opens a websocket based on the previous search
  componentWillMount: function() {
     if(loggedIn=="yes"){
       window.getMessage(this.state)
       window.getSocket(this.state)
     }
  },
  //sets the search string to that of the input value
  setSearchString: function(e) {
      var s = this.state
      s["searchString"] = e.target.value
      this.setState(s)
    },
  //performs a search based on the current state
  //opens a websocket based on the previous search
  handleSearchSubmit: function(e) {
      e.preventDefault()
      window.getMessage(this.state)
      window.getSocket(this.state)
  },
  render: function() {
  if(loggedIn == "yes") {
  //if logged-in, provides a search bar
  //search bar is capable of performing message searches based on the input
  return <li>
             <form className="navbar-form navbar-left" onSubmit={this.handleSearchSubmit}>
                 <div className="form-group">
                     <input type="text" name="search" className="form-control" placeholder="Search Chirper" value={this.state.searchString} onChange={this.setSearchString}/>
                 </div>
                 <button type="submit" className="btn btn-default">Search</button>
             </form>
         </li>
  } else {
  //if not logged in, provides a login form
  return  <li>
             <form className="navbar-form navbar-left" method="post" action="login">
                 <div className="form-group">
                     <input type="text" name="email" className="form-control" placeholder="Email" />
                     <input type="password" name="password" className="form-control" placeholder="Password" />
                 </div>
                 <button type="submit" className="btn btn-default">Login</button>
             </form>
         </li>
  }
  }
});

//NAVBAR
//the top navigation bar
var NavBar = React.createClass({
//sets the state for a default search
getInitialState: function() {
        return {
            searchString: ""
        }
    },
//submits a message search based on the state
//opens a websocket based on the search
handleSearchSubmit: function(e) {
      e.preventDefault()
      window.getMessage(this.state)
      window.getSocket(this.state)
},
//provides a hyperlink to a default message search displayed on the left
//invokes the search bar and user options for display on the right
render: function() {
    return <nav className="navbar navbar-default navbar-fixed-top">
               <div className="container-fluid">
                   <div className="navbar-header">
                       <button type="button" className="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
                           <span className="sr-only">Toggle navigation</span>
                           <span className="icon-bar"></span>
                           <span className="icon-bar"></span>
                           <span className="icon-bar"></span>
                       </button>
                       <a className="navbar-brand" href="#" onClick={this.handleSearchSubmit}>Chirper</a>
                       <p className="navbar-text">Tell everyone everything!</p>
                   </div>
                   <ul className="nav navbar-nav navbar-right">
                        <SearchBar />
                        <UserOptions />
                   </ul>
               </div>
           </nav>
}
});

//PAGE CONTENT
var PageContent = React.createClass({
    //initializes the state
    getInitialState: function() {
        return {
            post: ""
        }
    },
    //changes the state based on the value of the posted text
    setPost: function(e) {
       var s = this.state
       s["post"] = e.target.value
       this.setState(s)
    },
    //invokes a message post based on the current state
    //resets the current state
    handlePostSubmit: function(e) {
       e.preventDefault()
       window.postMessage(this.state)
       var s = this.state
       s["post"] = ""
       this.setState(s)
    },
    //if logged-in, gives a post form on the left of the page, and invokes a messagelist in the middle of the page
    render: function() {
         if(loggedIn == "yes") {
                return   <div className="container-fluid">
                            <div className="row">
                                <div className="col-md-4">
                                    <form onSubmit={this.handlePostSubmit}>
                                        <div className="form-group">
                                            <textarea ref="post" className="form-control" rows="col-md-4" name="message" maxLength="140" placeholder="Post to Chirper!" value={this.state.post} onChange={this.setPost}></textarea>
                                        </div>
                                        <button type="submit" className="btn btn-default">Post</button>
                                    </form>
                                </div>
                                <div className="col-md-4">
                                    <div>
                                      <MessageList messageList={window.message} />
                                    </div>
                                </div>
                            </div>

                        </div>
         }else{
             return <div></div>
        }
    }
});

//MESSAGE APP
var MessageApp = React.createClass({
  //renders both the navbar and content
  render: function() {
    return (
      <div>
        <NavBar />
        <PageContent />
      </div>
    );
  }
});

//renders the messageapp to the html element
var rerender = function() {
  React.render(<MessageApp />, mountNode);
}

rerender();