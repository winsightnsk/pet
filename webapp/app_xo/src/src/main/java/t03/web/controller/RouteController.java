package t03.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import t03.datasource.model.HumanEntity;
import t03.domain.DomainInterface;
import t03.domain.model.Game;
import t03.domain.model.MoveResult;
import t03.web.mapper.WebMapperComponent;
import t03.web.model.HashHuman;
import t03.web.model.WebBord;
import t03.web.service.AuthService;
import t03.web.service.SessionService;

@Controller @AllArgsConstructor
public class RouteController {

    private final DomainInterface domain;
    private final WebMapperComponent webMapper;
    private final AuthService authService;
    private final SessionService sessionServise;

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) return "redirect:/auth/";
        model.addAttribute("playerList", domain.getAllNames());
        return "home";
    }

    @PostMapping("/new/")
    public String handleCreatePlayer(
            @RequestParam String playerName,
            @RequestParam String playerPassword,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {
        Optional<HumanEntity> ohe = authService.HEbyNamePass(playerName, playerPassword, true);
        Optional<HashHuman> ohh = ohe.map(webMapper::heTohh);
        if (ohh.isEmpty()) {
            model.addAttribute("playerList", domain.getAllNames());
            model.addAttribute("error", "Не приняты Имя+Пароль");
            return "home";
        }
        HashHuman hh = ohh.get();
        UUID sessionId = sessionServise.createSession(hh, request);
        sessionServise.setSessionCookie(response, sessionId);
        
        return "redirect:/auth/";
    }

    @GetMapping("/game/opponent/join/{opponentID}")
    public String handleOpponentJoin (@PathVariable UUID opponentID,
                                      Model model,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        HashHuman hh = authService.checkAuth(model, request, response);
        if (domain.getGameByHid(opponentID).isPresent())
            throw new IllegalArgumentException("Приглашаемый игрок уже играет");
        Game game = domain.newGame(hh.getId(), opponentID);
        return "redirect:/game/"+game.getId();
    }

    @GetMapping("/game/opponent/list")
    public String handleGameOpponents (Model model,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        HashHuman hh = authService.checkAuth(model, request, response);
        List<HashHuman> oppList = domain.getOpponentList(hh);
        model.addAttribute("hh", hh);
        model.addAttribute("oppList", oppList);
        return "opponents";
    }

    @GetMapping("/game/drop")
    public String handleGameDrop(Model model,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        HashHuman hh = authService.checkAuth(model, request, response);
        domain.removeGame(hh);
        return "redirect:/auth/";
    }

    @GetMapping("/auth/")
    public String handleAuth(Model model,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        HashHuman hh = authService.checkAuth(model, request, response);
        model.addAttribute("hh", hh);
        Optional<Game> oGame = domain.getGameByHid(hh.getId());
        oGame.ifPresent(game -> model.addAttribute("idgame", game.getId()));
        return "auth";
    }

    @GetMapping("/logout")
    public String handleLogOut(Model model,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) for (Cookie cookie : cookies) {
            cookie.setValue(null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return "redirect:/";
    }

    @GetMapping("/game/new/ai")
    public String handleGameNewAI(Model model, HttpServletRequest request, HttpServletResponse response) {
        HashHuman hh = authService.checkAuth(model, request, response);
        return "redirect:/game/" + domain.newGame(hh.getId(), null).getId();
    }

    @GetMapping("/game/{gameId}")
    public String handleGame(@PathVariable UUID gameId, Model model,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        HashHuman hh = authService.checkAuth(model, request, response);
        checkGameAssign(hh.getId(), gameId);
        prepareWebModel(gameId, model);
        return "game";
    }

    @GetMapping("/turn/{position}/{gameId}")
    public String handleTurn(
            @PathVariable int position,
            @PathVariable UUID gameId,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {
        HashHuman hh = authService.checkAuth(model, request, response);
        Game game = domain.getGame(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Игра не найдена"));
        MoveResult moveResult = domain.makeMove(game, position, hh.getId());
        prepareWebModel(moveResult.getGame(), model);
        return "game";
    }


    private void checkGameAssign (UUID humanId, UUID gameId) {
        Optional<Game> oGame = domain.getGameByHid(humanId);
        if (oGame.isEmpty() || !oGame.get().getId().equals(gameId))
            throw new RuntimeException("Вы не учавствуете в этой игре");
    }

    public void prepareWebModel(UUID id, Model model) {
        Optional<Game> optGame = domain.getGame(id);
        if (optGame.isEmpty()) throw new IllegalArgumentException("Игра не найдена");
        prepareWebModel(optGame.get(), model);
    }
    public void prepareWebModel(Game game, Model model) {
        prepareWebModel(webMapper.gameToWebBoard(game), model);
    }
    public void prepareWebModel(WebBord wb, Model model) {
        model.addAttribute("cells", wb.getCells());
        model.addAttribute("gameId", wb.getGameId());
        model.addAttribute("xName", wb.getXName());
        model.addAttribute("oName", wb.getOName());
        model.addAttribute("gameOver", wb.isGameOver());
        model.addAttribute("message", wb.getMessage());
        model.addAttribute("turn", wb.isXTurn()
                ? wb.getXName()
                : wb.getOName());
    }

    @GetMapping("/favicon.ico")
    public void handleFavicon(HttpServletResponse response) throws IOException {
        String svg = """
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">
          <text y=".9em" font-size="90">🎮</text>
        </svg>
        """;

        response.setContentType("image/svg+xml");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(svg);
    }

}
