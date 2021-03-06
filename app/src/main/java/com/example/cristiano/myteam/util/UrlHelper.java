package com.example.cristiano.myteam.util;

/**
 * Created by Cristiano on 2017/4/17.
 *
 * this class offers the url to different APIs
 */

public class UrlHelper {
    //AWS URL
    private static final String AWS_URL = "http://ec2-52-90-49-22.compute-1.amazonaws.com:5000";
    // Heroku URL
    private static final String HEROKU_URL = "https://my-team-rest-api.herokuapp.com";
    //local testing URL
    private static final String HOME_URL = "http://192.168.1.4:5000";

//    private static final String URL = AWS_URL;
    private static final String URL = HEROKU_URL;
//    private static final String URL = HOME_URL;

    public static String urlLogin(){
        return URL + "/auth";
    }

    public static String urlRegister(){
        return URL + "/user";
    }

    public static String urlPutToken(int playerID){
        return URL + "/token/player/" + playerID;
    }

    public static String urlPlayerInfoByID(int playerID){
        return URL + "/player_info/id/" + playerID;
    }

    public static String urlPlayerByID(int playerID){
        return URL + "/player/id/" + playerID;
    }

    public static String urlPlayerByToken(){
        return URL + "/player/token";
    }

    public static String urlPlayerInfoByUser(int userID){
        return URL + "/player_info/user/" + userID;
    }

    public static String urlPlayerClubInfo(int clubID, int playerID){
        return URL + "/player_info/" + playerID + "/club/" + clubID;
    }

    public static String urlPostRegPlayer(int clubID){
        return URL + "/player/club/" + clubID;
    }

    public static String urlClubByID(int clubID){
        return URL + "/club/id/" + clubID;
    }

    public static String urlClubByName(String name){
        return URL + "/club/name/" + name;
    }

    public static String urlClubInfoByID(int clubID){
        return URL + "/club_info/id/" + clubID;
    }

    public static String urlEvent(){
        return URL + "/event";
    }

    public static String urlEventByClub(int clubID){
        return URL + "/event/club/" + clubID;
    }

    public static String urlMemberRequest(int clubID) {
        return URL + "/member/request/" + clubID;
    }

    public static String urlMemberManagement(int clubID, int playerID, boolean isPromotion) {
        if ( isPromotion ) {
            return URL + "/member/manage/club/" + clubID + "/player/" + playerID + "/promote/true";
        } else {
            return URL + "/member/manage/club/" + clubID + "/player/" + playerID + "/promote/false";
        }
    }

    public static String urlRegClubFromPlayer(int playerID){
        return URL + "/club/player/" + playerID;
    }

    public static String urlSquad() {
        return URL + "/squad";
    }

    public static String urlMembersByClub(int clubID){
        return URL + "/member/club/" + clubID;
    }

    public static String urlRegTournamentFromClub(int clubID, int playerID){
        return URL + "/tournament/club/" + clubID + "/player/" + playerID;
    }

    public static String urlTournamentSquad(int tournamentID, int clubID){
        return URL + "/tournament/" + tournamentID + "/club/" + clubID;
    }

    public static String urlStatsByTournamentClubPlayer(int tournamentID, int clubID, int playerID){
        return URL + "/stats/tournament/" + tournamentID + "/club/" + clubID + "/player/" + playerID;
    }

    public static String urlResultsByTournamentClub(int tournamentID, int clubID){
        return URL + "/result/tournament/"+tournamentID+"/club/" + clubID;
    }

    public static String urlTournamentsByClub(int clubID){
        return URL + "/tournament/club/" + clubID;
    }

    public static String urlStatsByTournamentClub(int tournamentID, int clubID) {
        return URL + "/stats/tournament/" + tournamentID + "/club/" + clubID;
    }

    public static String urlSquadByTournamentClub (int tournamentID, int clubID) {
        return URL + "/squad/tournament/" + tournamentID + "/club/" + clubID;
    }

    public static String urlChat(int tournamentID, int clubID, int receiverID, int senderID, int limit, int beforeID, int afterID) {
        return URL + "/chat/tournament/" + tournamentID + "/club/" + clubID + "/receiver/"
                + receiverID + "/sender/" + senderID + "/limit/" + limit
                + "/before/" + beforeID + "/after/" + afterID;
    }

    public static String urlChatByTournament(int tournamentID, int clubID) {
        return URL + "/chat/tournament/" + tournamentID + "/club/" + clubID;
    }

    public static String urlChatByClub(int clubID) {
        return URL + "/chat/club/" + clubID;
    }

    public static String urlPrivateChat(int receiverID) {
        return URL + "/chat/private/" + receiverID;
    }

    public static String urlLocationByClubPlayer(int clubID, int playerID) {
        return URL + "/location/club/" + clubID + "/player/" + playerID;
    }

    public static String urlLocationByClub(int clubID) {
        return URL + "/location/club/" + clubID;
    }

    public static String s3PlayerAvatarFolder(int playerID) {
        return "image/avatar/avatar_player_" + playerID;
    }

    /**
     *  store chat images in dedicated folders, use current time and sender to create unique filename
     */
    public static String s3ClubChatFolder(int clubID, int senderID) {
        return "image/chat/club/clubID_"+clubID+"/"
                + System.currentTimeMillis() + "_senderID_" + senderID;
    }

    public static String s3TournamentChatFolder(int tournamentID, int clubID, int senderID) {
        return "image/chat/tournament/tournamentID_" + tournamentID
                + "/club/clubID_" + clubID + "/"
                + System.currentTimeMillis() + "_senderID_" + senderID;
    }

    /**
     * Use folder /player1ID_player2ID/ to store private chat between player1 and player2,
     * where player1's ID is always smaller than player2's ID, so that we only need one folder
     * to store mutual messages between these two players
     */
    public static String s3PrivateChatFolder(int receiverID, int senderID) {
        return "image/chat/private/"
                + Math.min(receiverID,senderID) + "_" + Math.max(receiverID,senderID)
                + "/" + System.currentTimeMillis() + "_senderID_" + senderID;
    }
}
