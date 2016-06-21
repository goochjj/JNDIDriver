#!/usr/bin/perl

use POSIX;
use strict;


my $svnrev;
my $svnpath = 'svn+ssh://svnserve@devil.k12system.com/sapphire/XMessageFormat/trunk';

my $git;
foreach my $tst ("/usr/bin/git", "/usr/local/bin/git", "/opt/local/bin/git") {
  if ( -x $tst ) { $git = $tst; last; }
}
if (!$git) { print "Can't find git binary\n"; exit 1; }

my %outvars;

open(IN, "$git describe --tags --long |") or die "Could not pipe $@ $!";
my @gitver=<IN>;
close(IN);

if ($?==0 && $gitver[0] =~ /^\s*v(\d+(?:\.\d+)+)-(\d+)-g([A-Fa-f0-9]+)\s*$/) {
  print "Found Git version $gitver[0]\n";
  $outvars{"app.version"} = "$1";
  $outvars{"app.revision"} = "$2";
  $outvars{"app.gitHash"} = "$3";
  $outvars{"app.fullVersion"} = "$1.$2";
  $outvars{"app.gitFullVersion"} = "v$1-$2-g$3";
  $outvars{"app.textStatus"} = "normal";
  $outvars{"app.propStatus"} = "normal";
} else {
  open(IN, "$git log |") or die "Could not pipe $@ $!";
  while (<IN>) {
    chomp;
    next unless (/^\s*git-svn-id:\s*svn\+ssh:\/\/[^\@]+\@(\d+)/);
    $svnrev = $1;
    #print $_,"\n";
    last;
  }
  close(IN);
  print "Rev $svnrev\n";
  if (length($svnrev) < 1) {
    print "Could not find svn rev\n";
    exit 0;
  }


  foreach my $tok ([ 'app', '' ]) {
    my $pfx = $tok->[0];
    my $dirsfx = $tok->[1];

    my $version = undef;
    open(SVN, "svn propget version $svnpath/$dirsfx -r $svnrev|") or die "Could not pipe $@ $!";
    while (<SVN>) {
      chomp;
      next if (/^\s*$/);
      s/^\s+//g;
      s/\s+$//g;
      $version = $_;
    }
    close(SVN);

    if (!$version) {
      print "Version could not be retrieved!\n";
      exit(1);
    }

    $outvars{"$pfx.revision"} = $svnrev;
    $outvars{"$pfx.version"} = $version;
    $outvars{"$pfx.fullVersion"} = $version.".".$svnrev;
    $outvars{"$pfx.textStatus"} = "normal";
    $outvars{"$pfx.propStatus"} = "normal";

    open(SVN, "svn info $svnpath/$dirsfx -r $svnrev|") or die "Could not pipe $@ $!";
    while (<SVN>) {
      chomp;
      if (/^Last Changed Rev:\s+(\d+)/) { 
	$outvars{"$pfx.lastChangedRevision"} = $1; 
	$outvars{"$pfx.fullVersion"} = $version.".".$1;
      }
      if (/^Last Changed Author:\s+(\S+)/) { $outvars{"$pfx.lastChangedBy"} = $1; }
      if (/^Repository UUID:\s+(\S+)/) { $outvars{"$pfx.repoUUID"} = $1; }
      #print $_."\n";;
    }
    close(SVN);
  }
}

if (keys %outvars) {
  open(OUT, ">version.properties") or die "Could not create version.properties: $@ $!";
  print OUT "#".ctime(time());
  foreach my $key (sort keys %outvars) {
    print OUT $key."=".$outvars{$key}."\n";
  }
  close(OUT);
}
