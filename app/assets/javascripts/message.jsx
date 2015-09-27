var mountNode = document.getElementById('renderMessages')
var loggedIn = document.getElementById('loggedIn').value

var Message = React.createClass({
  render: function() {
    var obj = this.props.message
    var time = timeSince(obj.age)
    function timeSince(date) {

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
    }
    return <div className="panel panel-default">
               <div className="panel-heading">
                      <div className="user">{obj.email} - {time} ago</div>
               </div>
               <div className="panel-body">
                      <div className="message">{obj.message}</div>
               </div>
    </div>
  }
});

var MessageList = React.createClass({
  render: function() {
    return <ul>{this.props.messageList.map(function(item) {
      return <Message message={item} />
    })}</ul>;
  }
});

var UserOptions = React.createClass({
  render: function() {
  if(loggedIn == "yes") {
    var userEmail = document.getElementById('userEmail').value
    return <li className="dropdown">
                <a href="" className="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                    {userEmail}
                    <span className="caret"></span></a>
                <ul className="dropdown-menu">
                    <li><a href="user?email=@user.getEmail">My Chirps</a> </li>
                    <li><a href="viewactivesessions">Active Sessions</a></li>
                    <li><a href="logout">Logout</a></li>
                </ul>
            </li>
    }else {return <li className="dropdown">
                     <a href="" className="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                         Not logged in
                         <span className="caret"></span></a>
                         <ul className="dropdown-menu">
                             <li><a href="register">Register</a></li>
                             <li><a href="login">Login</a></li>
                         </ul>
                 </li>
         }
  }
});

var SearchBar = React.createClass({
  getInitialState: function() {
      return {
        searchString: ""
      }
    },
  setSearchString: function(e) {
      var s = this.state
      s["searchString"] = e.target.value
      this.setState(s)
    },
    handleSearchSubmit: function(e) {
      e.preventDefault()
      window.getMessage(this.state)
      window.getSocket(this.state)
    },
  render: function() {
  if(loggedIn == "yes") {
  return <li>
             <form className="navbar-form navbar-left" onSubmit={this.handleSearchSubmit}>
                 <div className="form-group">
                     <input type="text" name="search" className="form-control" placeholder="Search Chirper" value={this.state.searchString} onChange={this.setSearchString}/>
                 </div>
                 <button type="submit" className="btn btn-default">Search</button>
             </form>
         </li>
  } else {
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

var NavBar = React.createClass({
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
                           <a className="navbar-brand" href="/">Chirper</a>
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

var PageContent = React.createClass({
    getInitialState: function() {
        return {
            post: ""
        }
    },
    setPost: function(e) {
       var s = this.state
       s["post"] = e.target.value
       this.setState(s)
    },
    handlePostSubmit: function(e) {
       e.preventDefault()
       window.postMessage(this.state)
       var s = this.state
       s["post"] = ""
       this.setState(s)
    },
    render: function() {
     if(loggedIn == "yes") {
            return   <div className="container-fluid">
                        <div className="row">
                            <div className="col-md-4">
                                <form onSubmit={this.handlePostSubmit}>
                                    <div className="form-group">
                                        <textarea ref="post" className="form-control" rows="col-md-4" name="message" maxlength="140" placeholder="Post to Chirper!" value={this.state.post} onChange={this.setPost}></textarea>
                                    </div>
                                    <button type="submit" className="btn btn-default">Post</button>
                                </form>
                            </div>



                            <div className="col-md-8">
                                <div>
                                  <MessageList messageList={window.received} />
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

var MessageApp = React.createClass({
  render: function() {
    return (
      <div>
        <NavBar />
        <PageContent />
      </div>
    );
  }
});

var rerender = function() {
  React.render(<MessageApp />, mountNode);
}
rerender();