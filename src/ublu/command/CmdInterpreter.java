/*
 * Copyright (c) 2014, Absolute Performance, Inc. http://www.absolute-performance.com
 * Copyright (c) 2017, Jack J. Woehr jwoehr@softwoehr.com http://www.softwoehr.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ublu.command;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.RequestNotSupportedException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.logging.Level;
import ublu.util.ArgArray;

/**
 * BREAK from enclosing FOR
 *
 * @author jwoehr
 */
public class CmdInterpreter extends Command {

    {
        setNameAndDescription("interpreter", "/0 [-all | -getlocale | -setlocale ~@{lang} ~@{country} | -getmessage ~@{key} | -args | -opts | -arg ~@{nth} | -opt ~@{nth} | -optarg ~@{nth} | -allargs | -geterr | -getout | -seterr  ~@printstream | -setout  ~@printstream | -q,-query framedepth|instancedepth|forblock|breakissued|goublu|window] : info on and control of Ublu and the interpreter at the level this command is invoked");
    }

    enum OPS {
        ALL,
        GET_LOCALE,
        SET_LOCALE,
        GET_MESSAGE,
        OPT,
        OPTARG,
        ARG,
        ARGS,
        OPTS,
        ALLARGS,
        GETOUT,
        SETOUT,
        GETERR,
        SETERR,
        QUERY
    }

    ArgArray doInterpreter(ArgArray argArray) {
        OPS op = OPS.ALL;
        String language = null;
        String country = null;
        String messagekey = null;
        Integer nth = null;
        PrintStream ps = null;
        String qstring = null;
        while (argArray.hasDashCommand()) {
            String dashCommand = argArray.parseDashCommand();
            switch (dashCommand) {
                case "-to":
                    setDataDestfromArgArray(argArray);
                    break;
                case "-all":
                    op = OPS.ALL;
                    break;
                case "":
                    break;
                case "-getlocale":
                    op = OPS.GET_LOCALE;
                    break;
                case "-setlocale":
                    op = OPS.SET_LOCALE;
                    language = argArray.nextMaybeQuotationTuplePopStringTrim();
                    country = argArray.nextMaybeQuotationTuplePopStringTrim();
                    break;
                case "-getmessage":
                    op = OPS.GET_MESSAGE;
                    messagekey = argArray.nextMaybeQuotationTuplePopStringTrim();
                    break;
                case "-args":
                    op = OPS.ARGS;
                    break;
                case "-opts":
                    op = OPS.OPTS;
                    break;
                case "-opt":
                    op = OPS.OPT;
                    nth = argArray.nextIntMaybeQuotationTuplePopString();
                    break;
                case "-optarg":
                    op = OPS.OPTARG;
                    nth = argArray.nextIntMaybeQuotationTuplePopString();
                    break;
                case "-arg":
                    op = OPS.ARG;
                    nth = argArray.nextIntMaybeQuotationTuplePopString();
                    break;
                case "-allargs":
                    op = OPS.ALLARGS;
                    break;
                case "-geterr":
                    op = OPS.GETERR;
                    break;
                case "-getout":
                    op = OPS.GETOUT;
                    break;
                case "-seterr":
                    op = OPS.SETERR;
                    ps = argArray.nextTupleOrPop().value(PrintStream.class);
                    break;
                case "-setout":
                    op = OPS.SETOUT;
                    ps = argArray.nextTupleOrPop().value(PrintStream.class);
                    break;
                case "-q":
                case "-query":
                    op = OPS.QUERY;
                    qstring = argArray.nextMaybeQuotationTuplePopStringTrim().toLowerCase();
                    break;
                default:
                    unknownDashCommand(dashCommand);
            }
        }
        if (havingUnknownDashCommand()) {
            setCommandResult(COMMANDRESULT.FAILURE);
        } else {
            switch (op) {
                case ALL: {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Instance depth : ").append(getInterpreter().getInstanceDepth()).append("\n")
                            .append("Frame depth : ").append(getInterpreter().getFrameDepth()).append("\n")
                            .append("FOR block : ").append(getInterpreter().isForBlock()).append("\n")
                            .append("Break issued : ").append(getInterpreter().isBreakIssued()).append("\n")
                            .append("History filename : ").append(getInterpreter().getHistoryFileName()).append("\n")
                            .append("History manager : ").append(getInterpreter().getHistory()).append("\n")
                            .append("Locale info : ").append(getLocaleHelper()).append("\n")
                            .append("Running under Goublu: ").append(getInterpreter().isGoubluing()).append("\n")
                            .append("Running as a Window: ").append(getInterpreter().isWindowing());
                    try {
                        put(sb.toString());
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Exception putting interpreter info in " + getNameAndDescription(), ex);
                        setCommandResult(COMMANDRESULT.FAILURE);
                    }
                }
                break;
                case GET_LOCALE:
                    try {
                        put(getLocaleHelper().getCurrentLocale());
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Exception putting locale info in " + getNameAndDescription(), ex);
                        setCommandResult(COMMANDRESULT.FAILURE);
                    }
                    break;
                case GET_MESSAGE:
                    try {
                        put(getLocaleHelper().getString(messagekey));
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Exception putting locale info in " + getNameAndDescription(), ex);
                        setCommandResult(COMMANDRESULT.FAILURE);
                    }

                    break;
                case SET_LOCALE:
                    setLocale(language, country);
                    break;
                case ARGS:
                    try {
                        put(getUbluArgs().argumentCount());
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Error putting num args in " + getNameAndDescription(), ex);
                    }
                    break;
                case OPTS:
                    try {
                        put(getUbluArgs().optionCount());
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Error putting num opts in " + getNameAndDescription(), ex);
                    }
                    break;
                case ARG:
                    try {
                        put(getUbluArgs().nthArgument(nth));
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Error putting " + nth + " argument in " + getNameAndDescription(), ex);
                    }
                    break;
                case OPT:
                    try {
                        put(getUbluArgs().nthOption(nth).option);
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Error putting " + nth + " option in " + getNameAndDescription(), ex);
                    }
                    break;
                case OPTARG:
                    try {
                        put(getUbluArgs().nthOption(nth).argument);
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Error putting " + nth + " option argument in " + getNameAndDescription(), ex);
                    }
                    break;
                case ALLARGS:
                    try {
                        put(getUbluArgs().getArgumentsAsStringArray());
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Error putting all args in " + getNameAndDescription(), ex);
                    }
                    break;
                case GETERR:
                    try {
                        put(getInterpreter().getErroutStream());
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Error getting or putting err stream in " + getNameAndDescription(), ex);
                    }
                    break;
                case GETOUT:
                    try {
                        put(getInterpreter().getOutputStream());
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Error getting or putting out stream in " + getNameAndDescription(), ex);
                    }
                    break;
                case SETERR:
                    getInterpreter().setErroutStream(ps);
                    break;
                case SETOUT:
                    getInterpreter().setOutputStream(ps);
                    break;
                case QUERY:
                    try {
                        put(runQuery(qstring));
                    } catch (SQLException | IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException | RequestNotSupportedException ex) {
                        getLogger().log(Level.SEVERE, "Error getting or putting out stream in " + getNameAndDescription(), ex);
                    }
                    break;
            }
        }
        return argArray;
    }

    private Object runQuery(String q) {
        Object o = null;
        switch (q) {
            case "framedepth":
                o = getInterpreter().getFrameDepth();
                break;
            case "instancedepth":
                o = getInterpreter().getInstanceDepth();
                break;
            case "forblock":
                o = getInterpreter().isForBlock();
                break;
            case "breakissued":
                o = getInterpreter().isBreakIssued();
                break;
            case "goublu":
                o = getInterpreter().isGoubluing();
                break;
            case "window":
                o = getInterpreter().isWindowing();
                break;
        }
        return o;
    }

    @Override
    public ArgArray cmd(ArgArray args
    ) {
        reinit();
        return doInterpreter(args);
    }

    @Override
    public COMMANDRESULT getResult() {
        return getCommandResult();
    }
}
